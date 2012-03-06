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

public class Version implements Comparable<Version> {
	private String str;
	private int major;
	private int minor;
	private String rev;

	public Version(String str) {
		this.str = str;

		String[] tokens = str.split("[\\.-]");
		major = Integer.parseInt(tokens[0]);
		minor = Integer.parseInt(tokens[1]);

		rev = "";
		for (int i = 2; i < tokens.length; i++) {
			if (i != 2)
				rev += ".";

			rev += tokens[i];
		}
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	@Override
	public String toString() {
		return str;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Version other = (Version) o;
		if (str == null) {
			if (other.str != null)
				return false;
		} else if (!str.equals(other.str))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((str == null) ? 0 : str.hashCode());
		return result;
	}

	@Override
	public int compareTo(Version o) {
		if (o == null)
			throw new NullPointerException();

		if (major != o.major)
			return major - o.major;

		if (minor != o.minor)
			return minor - o.minor;

		try {
			int lhs = Integer.parseInt(rev);
			int rhs = Integer.parseInt(o.rev);
			return lhs - rhs;
		} catch (NumberFormatException e) {
			if (rev == null && o.rev == null)
				return 0;

			if (rev == null)
				return -1;
			else if (o.rev == null)
				return 1;

			return rev.compareTo(o.rev);
		}
	}
}
