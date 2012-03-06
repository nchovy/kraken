/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.api;

public class VersionRange {
	private Version low;
	private Version high;

	public VersionRange(Version one) {
		this(one, one);
	}

	public VersionRange(Version low, Version high) {
		this.low = low;
		this.high = high;
	}

	public Version getLow() {
		return low;
	}

	public void setLow(Version low) {
		this.low = low;
	}

	public Version getHigh() {
		return high;
	}

	public void setHigh(Version high) {
		this.high = high;
	}

	public boolean contains(Version version) {
		if (low.compareTo(version) <= 0 && version.compareTo(high) <= 0)
			return true;

		return false;
	}

	@Override
	public String toString() {
		return String.format("[%s, %s]", low, high);
	}

}
