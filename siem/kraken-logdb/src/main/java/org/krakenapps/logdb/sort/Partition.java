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

import java.util.LinkedList;
import java.util.List;

public class Partition {
	private List<SortedRunRange> runRanges;

	public Partition() {
		this.runRanges = new LinkedList<SortedRunRange>();
	}

	public List<SortedRunRange> getRunRanges() {
		return runRanges;
	}

	public int length() {
		int total = 0;
		for (SortedRunRange range : runRanges)
			total += range.getTo() - range.getFrom() + 1;
		return total;
	}

	@Override
	public String toString() {
		return "len=" + length() + ", " + runRanges.toString();
	}

}
