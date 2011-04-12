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
public class DefaultMessageSpecVersionRange implements MessageSpecVersionRange {
	private final MessageSpecVersion lowerBound;
	private final MessageSpecVersion upperBound;
	private final String representation;

	public DefaultMessageSpecVersionRange(MessageSpecVersion v1, MessageSpecVersion v2) {
		if (isLowerThanOrEqual(v1, v2)) {
			this.lowerBound = v1;
			this.upperBound = v2;
		} else {
			this.lowerBound = v2;
			this.upperBound = v1;
		}

		if (lowerBound.equals(upperBound))
			representation = "(" + upperBound + ")";
		else
			representation = "(" + lowerBound + " ~ " + upperBound + ")";
	}

	@Override
	public MessageSpecVersion getLowerBound() {
		return lowerBound;
	}

	@Override
	public MessageSpecVersion getUpperBound() {
		return upperBound;
	}

	private boolean isLowerThanOrEqual(MessageSpecVersion lhs, MessageSpecVersion rhs) {
		if (lhs.getMajorVersion() < rhs.getMajorVersion())
			return true;

		if ((lhs.getMajorVersion() == rhs.getMajorVersion()) && lhs.getMinorVersion() <= rhs.getMinorVersion()) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isSubsetOf(MessageSpecVersionRange range) {
		return isLowerThanOrEqual(range.getLowerBound(), lowerBound)
				&& isLowerThanOrEqual(upperBound, range.getUpperBound());
	}

	@Override
	public boolean isSupersetOf(MessageSpecVersionRange range) {
		return isLowerThanOrEqual(lowerBound, range.getLowerBound())
				&& isLowerThanOrEqual(range.getUpperBound(), upperBound);
	}

	@Override
	public String toString() {
		return representation;
	}

}
