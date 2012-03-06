/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.iptables.match;

public class PortRange {
	private int from;
	private int to;
	private boolean inverted;

	public PortRange(int from, int to) {
		this(from, to, false);
	}

	public PortRange(int from, int to, boolean inverted) {
		this.from = from;
		this.to = to;
		this.inverted = inverted;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public boolean isInverted() {
		return inverted;
	}

	public boolean isInRange(int port) {
		boolean test = (from <= port) && (port <= to);
		if (inverted)
			return !test;
		return test;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + from;
		result = prime * result + (inverted ? 1231 : 1237);
		result = prime * result + to;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PortRange other = (PortRange) obj;
		if (from != other.from)
			return false;
		if (inverted != other.inverted)
			return false;
		if (to != other.to)
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (from == to)
			return Integer.toString(from);

		return from + ":" + to;
	}

}
