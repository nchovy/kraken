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

public class SortedRunRange {
	private SortedRun run;
	private int from;
	private int to;

	public SortedRunRange() {
	}

	public SortedRunRange(SortedRun run, int from, int to) {
		this.run = run;
		this.from = from;
		this.to = to;
	}

	public SortedRun getRun() {
		return run;
	}

	public void setRun(SortedRun run) {
		this.run = run;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getTo() {
		return to;
	}

	public void setTo(int to) {
		this.to = to;
	}

	public int length() {
		return to - from + 1;
	}

	@Override
	public String toString() {
		return "[#" + run + ", from=" + from + ", to=" + to + "]";
	}

}
