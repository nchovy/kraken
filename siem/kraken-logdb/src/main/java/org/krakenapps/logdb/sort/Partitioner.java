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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class Partitioner {
	private Comparator<Item> comparator;

	public Partitioner(Comparator<Item> comparator) {
		this.comparator = comparator;
	}

	public List<Partition> partition(int count, List<SortedRun> runs) {
		if ((count & (count - 1)) > 0)
			throw new IllegalArgumentException("count should be power of 2, count=" + count);

		List<SortedRunStatus> sortedRuns = new LinkedList<SortedRunStatus>();
		for (SortedRun run : runs)
			sortedRuns.add(new SortedRunStatus(run));

		List<Partition> partitions = new ArrayList<Partition>();
		divide(count, sortedRuns, partitions);
		return partitions;
	}

	private void divide(int count, List<SortedRunStatus> sortedRuns, List<Partition> partitions) {
		count /= 2;

		// find median of each runs
		PriorityQueue<Median> q = new PriorityQueue<Median>();
		for (SortedRunStatus status : sortedRuns) {
			status.boundary = status.left + (status.right - status.left) / 2;
			status.median = status.run.get(status.boundary);
			q.add(new Median(status, status.boundary, status.median));
		}

		// sort by median
		Item medianOfMedian = null;
		int runCount = sortedRuns.size();
		int half = (runCount - 1) / 2;
		int i = 0;
		while (true) {
			Median m = q.poll();
			if (m == null)
				break;

			if (i <= half) {
				m.runStatus.medianLeft = Math.min(m.offset, m.runStatus.medianRight);
				if (i == half) {
					medianOfMedian = m.value;
				}
			} else {
				m.runStatus.medianRight = Math.max(m.offset - 1, m.runStatus.medianLeft);
			}

			i++;
		}

		// ensure median of median boundary for each run
		for (SortedRunStatus r : sortedRuns)
			findBoundary(medianOfMedian, r);

		if (count == 1) {
			Partition p1 = new Partition();
			Partition p2 = new Partition();

			for (SortedRunStatus status : sortedRuns) {
				if (status.left < status.boundary)
					p1.getRunRanges().add(new SortedRunRange(status.run, status.left, status.boundary - 1));
				if (status.boundary <= status.right)
					p2.getRunRanges().add(new SortedRunRange(status.run, status.boundary, status.right));
			}

			partitions.add(p1);
			partitions.add(p2);
		} else {
			List<SortedRunStatus> leftRuns = new LinkedList<SortedRunStatus>();
			List<SortedRunStatus> rightRuns = new LinkedList<SortedRunStatus>();

			for (SortedRunStatus status : sortedRuns) {
				if (status.left < status.boundary)
					leftRuns.add(new SortedRunStatus(status.run, status.left, status.boundary - 1));
				if (status.boundary <= status.right)
					rightRuns.add(new SortedRunStatus(status.run, status.boundary, status.right));
			}

			divide(count, leftRuns, partitions);
			divide(count, rightRuns, partitions);
		}
	}

	private void findBoundary(Item medianOfMedian, SortedRunStatus runStatus) {
		int left = runStatus.medianLeft;
		int right = runStatus.medianRight;
		int mid = 0;
		Item value = null;

		while (left <= right) {
			mid = left + ((right - left) / 2);
			value = runStatus.run.get(mid);
			int ret = comparator.compare(medianOfMedian, value);

			if (ret < 0) {
				right = mid - 1;
			} else if (ret > 0) {
				left = mid + 1;
			} else {
				break;
			}
		}

		// check predicate
		while (left <= mid) {
			value = runStatus.run.get(mid);
			int ret = comparator.compare(medianOfMedian, value);
			if (ret < 0)
				mid--;
			else
				break;
		}

		runStatus.boundary = mid + 1;
	}

	private class Median implements Comparable<Median> {
		private SortedRunStatus runStatus;
		private int offset;
		private Item value;

		public Median(SortedRunStatus runStatus, int offset, Item value) {
			this.runStatus = runStatus;
			this.offset = offset;
			this.value = value;
		}

		@Override
		public int compareTo(Median o) {
			return comparator.compare(value, o.value);
		}

		@Override
		public String toString() {
			return "#" + runStatus.run + ", offset=" + offset + ", median=" + value;
		}
	}

	private static class SortedRunStatus {
		private SortedRun run;
		private int left;
		private int right;
		private int boundary;
		private Item median;

		// median binary search range
		private int medianLeft;
		private int medianRight;

		public SortedRunStatus(SortedRun run) {
			this.run = run;
			this.left = 0;
			this.right = run.length() - 1;
			this.medianLeft = 0;
			this.medianRight = this.right;
		}

		public SortedRunStatus(SortedRun run, int left, int right) {
			this.run = run;
			this.left = left;
			this.right = right;
			this.medianLeft = left;
			this.medianRight = right;
		}

		@Override
		public String toString() {
			return "#" + run + ", boundary=" + boundary + ", median=" + median;
		}

	}
}
