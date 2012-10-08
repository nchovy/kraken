/*
 * Copyright 2012 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.logdb.sort;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelMergeSorter {
	private Queue<Run> runs = new LinkedBlockingDeque<Run>();
	private Queue<PartitionMergeTask> merges = new LinkedBlockingQueue<PartitionMergeTask>();
	private LinkedList<Object> buffer;
	private Comparator<Object> comparer;
	private int runLength = 400000;
	private AtomicInteger runIndexer;
	private AtomicInteger flushTasks;
	private AtomicInteger mergeTasks;
	private AtomicInteger cacheCount;
	private Object flushDoneSignal = new Object();
	private Object mergeDoneSignal = new Object();
	private ExecutorService executor;

	public ParallelMergeSorter(Comparator<Object> comparer) {
		this.comparer = comparer;
		this.buffer = new LinkedList<Object>();
		this.runIndexer = new AtomicInteger();
		this.flushTasks = new AtomicInteger();
		this.mergeTasks = new AtomicInteger();
		this.executor = new ThreadPoolExecutor(8, 8, 10, TimeUnit.SECONDS, new LimitedQueue<Runnable>(8));
		this.cacheCount = new AtomicInteger(2000000);
	}

	public class LimitedQueue<E> extends ArrayBlockingQueue<E> {
		private static final long serialVersionUID = 1L;

		public LimitedQueue(int maxSize) {
			super(maxSize);
		}

		@Override
		public boolean offer(E e) {
			// turn offer() and add() into a blocking calls (unless interrupted)
			try {
				put(e);
				return true;
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
			return false;
		}

	}

	public void add(Object item) throws IOException {
		buffer.add(item);
		if (buffer.size() >= runLength)
			flushRun();
	}

	public void addAll(List<? extends Object> items) throws IOException {
		buffer.addAll(items);
		if (buffer.size() >= runLength)
			flushRun();
	}

	private void flushRun() throws IOException, FileNotFoundException {
		LinkedList<Object> buffered = buffer;
		if (buffered.isEmpty())
			return;

		buffer = new LinkedList<Object>();
		flushTasks.incrementAndGet();
		executor.submit(new FlushWorker(buffered));
	}

	public CloseableIterator sort() throws IOException {
		// flush rest objects
		flushRun();
		buffer = null;

		// wait flush done
		while (flushTasks.get() != 0) {
			try {
				synchronized (flushDoneSignal) {
					flushDoneSignal.wait();
					System.out.println("remaining runs: " + runs.size() + ", task count: " + flushTasks.get());
				}
			} catch (InterruptedException e) {
				System.out.println("interrupted ");
			}
		}

		// partition
		System.out.println("start partitioning...");
		long begin = new Date().getTime();
		Partitioner partitioner = new Partitioner(comparer);
		List<SortedRun> sortedRuns = new LinkedList<SortedRun>();
		for (Run run : runs)
			sortedRuns.add(new SortedRunImpl(run));

		runs.clear();

		int partitionCount = Runtime.getRuntime().availableProcessors();
		List<Partition> partitions = partitioner.partition(partitionCount, sortedRuns);
		for (SortedRun r : sortedRuns)
			((SortedRunImpl) r).close();

		long elapsed = new Date().getTime() - begin;
		System.out.println("partition completed in " + elapsed);

		// n-way merge
		Run run = mergeAll(partitions);
		System.out.println("last result: " + run.dataFile.getAbsolutePath());
		executor.shutdown();

		if (run.cached != null)
			return new CacheRunIterator(run.cached.iterator());
		else
			return new FileRunIterator(run.dataFile);
	}

	private static class SortedRunImpl implements SortedRun {
		private RunInputRandomAccess ra;

		public SortedRunImpl(Run run) throws IOException {
			this.ra = new RunInputRandomAccess(run);
		}

		@Override
		public int length() {
			return ra.run.length;
		}

		@Override
		public Object get(int offset) {
			try {
				return ra.get(offset);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

		public void close() {
			ra.close();
		}
	}

	private Run mergeAll(List<Partition> partitions) throws IOException {
		// enqueue partition merge
		int id = 0;
		for (Partition p : partitions) {
			List<Run> runParts = new LinkedList<Run>();
			for (SortedRunRange range : p.getRunRanges()) {
				SortedRunImpl ri = (SortedRunImpl) range.getRun();
				Run run = ri.ra.run;
				int newId = runIndexer.incrementAndGet();

				if (run.cached != null) {
					List<Object> sublist = run.cached.subList(range.getFrom(), range.getTo() + 1);
					Run r = new Run(newId, sublist);
					runParts.add(r);
				} else {
					Run r = new Run(newId, range.length(), run.indexFile, run.dataFile, range.getFrom());
					runParts.add(r);
				}
			}

			PartitionMergeTask task = new PartitionMergeTask(id++, runParts);
			merges.add(task);
			mergeTasks.incrementAndGet();
			executor.submit(new MergeWorker(task));
		}

		// wait partition merge
		while (true) {
			if (mergeTasks.get() == 0)
				break;

			try {
				synchronized (mergeDoneSignal) {
					mergeDoneSignal.wait();
					System.out.println("remaining runs: " + runs.size() + ", task count: " + mergeTasks.get());
				}
			} catch (InterruptedException e) {
				System.out.println("interrupted ");
			}
		}

		// final merge
		ArrayList<PartitionMergeTask> l = new ArrayList<PartitionMergeTask>();
		while (true) {
			PartitionMergeTask t = merges.poll();
			if (t == null)
				break;
			l.add(t);
		}

		Collections.sort(l);

		ArrayList<Run> finalRuns = new ArrayList<Run>();
		for (PartitionMergeTask t : l) {
			finalRuns.add(t.output);
		}

		return concat(finalRuns);
	}

	private class FlushWorker implements Runnable {
		private LinkedList<Object> buffered;

		public FlushWorker(LinkedList<Object> list) {
			buffered = list;
		}

		@Override
		public void run() {
			try {
				doFlush();
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				flushTasks.decrementAndGet();
				synchronized (flushDoneSignal) {
					flushDoneSignal.notifyAll();
				}
			}
		}

		private void doFlush() throws IOException {
			Collections.sort(buffered, comparer);

			int id = runIndexer.incrementAndGet();
			RunOutput out = new RunOutput(id, buffered.size(), cacheCount);
			try {
				for (Object o : buffered)
					out.write(o);
			} finally {
				Run run = out.finish();
				runs.add(run);
			}
		}
	}

	private class MergeWorker implements Runnable {
		private PartitionMergeTask task;

		public MergeWorker(PartitionMergeTask task) {
			this.task = task;
		}

		@Override
		public void run() {
			try {
				Run merged = merge(task.runs);
				System.out.println("merged run " + merged + ", input runs: " + task.runs);
				task.output = merged;
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				mergeTasks.decrementAndGet();

				synchronized (mergeDoneSignal) {
					mergeDoneSignal.notifyAll();
				}
			}
		}
	}

	private void writeRestObjects(RunInput in, RunOutput out) throws IOException {
		while (in.hasNext()) {
			out.write(in.next());
		}
	}

	private Run concat(List<Run> finalRuns) throws IOException {
		RunOutput out = null;
		List<RunInput> inputs = new LinkedList<RunInput>();
		try {
			int total = 0;
			for (Run r : finalRuns) {
				total += r.length;
				inputs.add(new RunInput(r, cacheCount));
			}

			int id = runIndexer.incrementAndGet();
			out = new RunOutput(id, total, cacheCount, true);

			byte[] b = new byte[640 * 1024];
			for (RunInput in : inputs) {
				if (in.cachedIt != null) {
					writeRestObjects(in, out);
					out.dataBos.flush();
				} else {
					// fast file copy
					while (true) {
						int readBytes = in.bis.read(b);
						if (readBytes <= 0)
							break;
						out.dataBos.write(b, 0, readBytes);
					}
				}
			}

		} finally {
			for (RunInput input : inputs)
				input.purge();

			if (out != null)
				return out.finish();
		}

		return null;
	}

	private Run merge(List<Run> runs) throws IOException {
		if (runs.size() == 0)
			throw new IllegalArgumentException("runs should not be empty");

		if (runs.size() == 1)
			return runs.get(0);

		System.out.println("begin " + runs.size() + "way merge: " + runs);
		ArrayList<RunInput> inputs = new ArrayList<RunInput>();
		PriorityQueue<RunItem> q = new PriorityQueue<RunItem>(runs.size(), new RunItemComparater());
		RunOutput r3 = null;
		try {
			int total = 0;
			for (Run r : runs) {
				inputs.add(new RunInput(r, cacheCount));
				total += r.length;
			}

			int id = runIndexer.incrementAndGet();
			r3 = new RunOutput(id, total, cacheCount, true);

			while (true) {
				// load next inputs
				for (RunInput input : inputs) {
					if (input.loaded == null && input.hasNext()) {
						input.loaded = input.next();
						q.add(new RunItem(input, input.loaded));
					}
				}

				RunItem item = q.poll();
				if (item == null)
					break;

				r3.write(item.item);
				item.runInput.loaded = null;
			}

		} finally {
			for (RunInput input : inputs)
				input.purge();

			if (r3 != null)
				return r3.finish();
		}

		System.out.println("bug check!!!");
		return null;
	}

	private class RunItemComparater implements Comparator<RunItem> {

		@Override
		public int compare(RunItem o1, RunItem o2) {
			return comparer.compare(o1.item, o2.item);
		}

	}

	private static class RunItem {
		private RunInput runInput;
		private Object item;

		public RunItem(RunInput runInput, Object item) {
			this.runInput = runInput;
			this.item = item;
		}
	}

	private class PartitionMergeTask implements Comparable<PartitionMergeTask> {
		private int id;
		private List<Run> runs;
		private Run output;

		public PartitionMergeTask(int id, List<Run> runs) {
			this.id = id;
			this.runs = runs;
		}

		@Override
		public int compareTo(PartitionMergeTask o) {
			return id - o.id;
		}
	}
}
