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
public class DefaultMessageSpec implements MessageSpec {
	private final String name;
	private final String description;
	private final MessageSpecVersionRange range;
	private final String representation;

	public DefaultMessageSpec(String name, MessageSpecVersion version) {
		this(name, null, version);
	}

	public DefaultMessageSpec(String name, int majorVersion, int minorVersion) {
		this(name, new DefaultMessageSpecVersion(majorVersion, minorVersion));
	}

	public DefaultMessageSpec(String name, String description, MessageSpecVersion version) {
		this(name, description, new DefaultMessageSpecVersionRange(version, version));
	}

	public DefaultMessageSpec(String name, String description, MessageSpecVersionRange range) {
		this.name = name;
		this.description = description;
		this.range = range;

		representation = name + " " + range;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public MessageSpecVersionRange getVersionRange() {
		return range;
	}

	@Override
	public MessageSpecVersion getLatestVersion() {
		return range.getUpperBound();
	}

	@Override
	public boolean isSubsetOf(MessageSpec spec) {
		if (!name.equals(spec.getName()))
			return false;

		if (!range.isSubsetOf(spec.getVersionRange()))
			return false;

		return true;
	}

	@Override
	public boolean isSupersetOf(MessageSpec spec) {
		if (!name.equals(spec.getName()))
			return false;

		if (!range.isSupersetOf(spec.getVersionRange()))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return representation;
	}

}
