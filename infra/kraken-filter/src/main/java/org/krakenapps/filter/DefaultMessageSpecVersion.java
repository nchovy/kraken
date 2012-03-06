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
package org.krakenapps.filter;

/**
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class DefaultMessageSpecVersion implements MessageSpecVersion {
	private final int majorVersion;
	private final int minorVersion;
	private final String representation;

	public DefaultMessageSpecVersion(int majorVersion, int minorVersion) {
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		representation = majorVersion + "." + minorVersion;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MessageSpecVersion))
			return false;

		MessageSpecVersion other = (MessageSpecVersion) obj;

		return this.majorVersion == other.getMajorVersion() && this.minorVersion == other.getMinorVersion();
	}
	
	@Override
	public String toString() {
		return representation; 
	}

	@Override
	public int getMajorVersion() {
		return majorVersion;
	}

	@Override
	public int getMinorVersion() {
		return minorVersion;
	}

	@Override
	public boolean isInRange(MessageSpecVersionRange range) {
		if (isLowerThan(this, range.getLowerBound()))
			return false;

		if (isLowerThan(range.getUpperBound(), this))
			return false;

		return true;
	}

	private boolean isLowerThan(MessageSpecVersion lhs, MessageSpecVersion rhs) {
		if (lhs.getMajorVersion() < rhs.getMajorVersion())
			return true;

		if ((lhs.getMajorVersion() == rhs.getMajorVersion()) && lhs.getMinorVersion() < rhs.getMinorVersion()) {
			return true;
		}

		return false;
	}

}
