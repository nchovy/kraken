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
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParallelMergeSorter {
	private final Logger logger = LoggerFactory.getLogger(ParallelMergeSorter.class);
	private Queue<Run> runs = new LinkedBlockingDeque<Run>();
	private Queue<PartitionMergeTask> merges = new LinkedBlockingQueue<PartitionMergeTask>();
	private LinkedList<Item> buffer;
	private Comparator<Item> comparer;
	private int runLength = 20000;
	private AtomicInteger runIndexer;
	private volatile int flushTaskCount;
	private AtomicInteger cacheCount;
	private Object flushDoneSignal = new Object();
	private ExecutorService executor;
	private CyclicBarrier mergeBarrier;

	public ParallelMergeSorter(Comparator<Item> comparer) {
		this.comparer = comparer;
		this.buffer = new LinkedList<Item>();
		this.runIndexer = new AtomicInteger();
		this.executor = new ThreadPoolExecutor(8, 8, 10, TimeUnit.SECONDS, new LimitedQueue<Runnable>(8));
		this.cacheCount = new AtomicInteger(10000);
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

	public void add(Item item) throws IOException {
		buffer.add(item);
		if (buffer.size() >= runLength)
			flushRun();
	}

	public void addAll(List<? extends Item> items) throws IOException {
		buffer.addAll(items);
		if (buffer.size() >= runLength)
			flushRun();
	}

	private void flushRun() throws IOException, FileNotFoundException {
		LinkedList<Item> buffered = buffer;
		if (buffered.isEmpty())
			return;

		buffer = new LinkedList<Item>();
		synchronized (flushDoneSignal) {
			flushTaskCount++;
		}
		executor.submit(new FlushWorker(buffered));
	}

	public CloseableIterator sort() throws IOException {
		// flush rest objects
		flushRun();
		buffer = null;

		// wait flush done
		while (true) {
			synchronized (flushDoneSignal) {
				if (flushTaskCount == 0)
					break;

				try {
					flushDoneSignal.wait();
				} catch (InterruptedException e) {
				}
				logger.debug("kraken logdb: remaining runs {}, task count: {}", runs.size(), flushTaskCount);
			}
		}

		// partition
		logger.debug("kraken logdb: start partitioning");
		long begin = new Date().getTime();
		Partitioner partitioner = new Partitioner(comparer);
		List<SortedRun> sortedRuns = new LinkedList<SortedRun>();
		for (Run run : runs)
			sortedRuns.add(new SortedRunImpl(run));

		runs.clear();

		int partitionCount = getProperPartitionCount();
		List<Partition> partitions = partitioner.partition(partitionCount, sortedRuns);
		for (SortedRun r : sortedRuns)
			((SortedRunImpl) r).close();

		long elapsed = new Date().getTime() - begin;
		logger.debug("kraken logdb: [{}] partitioning completed in {}ms", partitionCount, elapsed);

		// n-way merge
		Run run = mergeAll(partitions);
		executor.shutdown();

		if (run.cached != null)
			return new CacheRunIterator(run.cached.iterator());
		else
			return new FileRunIterator(run.dataFile);
	}

	private static int getProperPartitionCount() {
		int processors = Runtime.getRuntime().availableProcessors();
		int count = 2;
		while (count < processors)
			count <<= 1;

		return count;
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
		public Item get(int offset) {
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

		List<PartitionMergeTask> tasks = new ArrayList<PartitionMergeTask>();
		for (Partition p : partitions) {
			List<Run> runParts = new LinkedList<Run>();
			for (SortedRunRange range : p.getRunRanges()) {
				SortedRunImpl ri = (SortedRunImpl) range.getRun();
				Run run = ri.ra.run;
				int newId = runIndexer.incrementAndGet();

				if (run.cached != null) {
					List<Item> sublist = run.cached.subList(range.getFrom(), range.getTo() + 1);
					Run r = new Run(newId, sublist);
					runParts.add(r);
				} else {
					Run r = new Run(newId, range.length(), run.indexFile.share(), run.dataFile.share(), range.getFrom());
					runParts.add(r);
				}
			}

			if (runParts.size() > 0) {
				PartitionMergeTask task = new PartitionMergeTask(id++, runParts);
				tasks.add(task);
			}
		}

		mergeBarrier = new CyclicBarrier(tasks.size() + 1);
		for (PartitionMergeTask task : tasks) {
			merges.add(task);
			executor.submit(new MergeWorker(task));
		}

		// wait partition merge
		try {
			mergeBarrier.await();
		} catch (InterruptedException e) {
		} catch (BrokenBarrierException e) {
			logger.error("kraken logdb: barrier assumption fail", e);
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
		private LinkedList<Item> buffered;

		public FlushWorker(LinkedList<Item> list) {
			buffered = list;
		}

		@Override
		public void run() {
			try {
				doFlush();
			} catch (Throwable t) {
				logger.error("kraken logdb: failed to flush", t);
			} finally {
				synchronized (flushDoneSignal) {
					flushTaskCount--;
					flushDoneSignal.notifyAll();
				}
			}
		}

		private void doFlush() throws IOException {
			Collections.sort(buffered, comparer);

			int id = runIndexer.incrementAndGet();
			RunOutput out = new RunOutput(id, buffered.size(), cacheCount);
			try {
				for (Item o : buffered)
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
				Run merged = mergeRuns(task.runs);
				logger.debug("kraken logdb: merged run {}, input runs: {}", merged, task.runs);

				task.output = merged;
			} catch (Throwable t) {
				logger.error("kraken logdb: failed to merge " + task.runs, t);
			} finally {
				try {
					mergeBarrier.await();
				} catch (InterruptedException e) {
				} catch (BrokenBarrierException e) {
					logger.error("kraken logdb: merge barrier assumption fail", e);
				}
			}
		}

		private Run mergeRuns(List<Run> runs) throws IOException {
			if (runs.size() == 1)
				return runs.get(0);

			List<Run> phase = new ArrayList<Run>();
			for (int i = 0; i < 8; i++) {
				if (!runs.isEmpty())
					phase.add(runs.remove(0));
			}

			runs.add(merge(phase));
			return mergeRuns(runs);
		}

	}

	private void writeRestObjects(RunInput in, RunOutput out) throws IOException {
		int count = 0;
		while (in.hasNext()) {
			out.write(in.next());
			count++;
		}
		logger.debug("kraken logdb: final output writing from run #{}, count={}", in.getId(), count);
	}

	private Run concat(List<Run> finalRuns) throws IOException {
		logger.debug("kraken logdb: concat begins");
		RunOutput out = null;
		List<RunInput> inputs = new LinkedList<RunInput>();
		try {
			int total = 0;
			for (Run r : finalRuns) {
				total += r.length;
				inputs.add(new RunInput(r, cacheCount));
				logger.debug("kraken logdb: concat run #{}", r.id);
			}

			int id = runIndexer.incrementAndGet();
			out = new RunOutput(id, total, cacheCount, true);

			for (RunInput in : inputs) {
				writeRestObjects(in, out);
				if (out.dataBos != null)
					out.dataBos.flush();
			}

		} catch (Exception e) {
			logger.error("kraken logdb: failed to concat " + finalRuns, e);
		} finally {
			for (RunInput input : inputs) {
				input.purge();
			}

			if (out != null)
				return out.finish();
		}

		return null;
	}

	private Run merge(List<Run> runs) throws IOException {
		if (runs.size() == 0)
			throw new IllegalArgumentException("runs should not be empty");

		if (runs.size() == 1) {
			return runs.get(0);
		}

		logger.debug("kraken logdb: begin {}way merge, {}", runs.size(), runs);
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

		logger.error("kraken logdb: merge cannot reach here, bug check!");
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
		private Item item;

		public RunItem(RunInput runInput, Item item) {
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
