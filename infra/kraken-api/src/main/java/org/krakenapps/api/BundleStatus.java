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

import java.util.Date;

public class BundleStatus {
	private String symbolicName;
	private String version;
	private int state;
	private Date buildTimestamp;

	public BundleStatus(String symbolcName, String version, int state, Date lastModified) {
		this.symbolicName = symbolcName;
		this.version = version;
		this.state = state;
		this.buildTimestamp = lastModified;
	}

	public String getSymbolicName() {
		return symbolicName;
	}

	public String getVersion() {
		return version;
	}

	public String getStateName() {
		return toStateName(this.state);
	}

	private String toStateName(int state) {
		switch (state) {
		case 0x20:
			return "ACTIVE";
		case 0x2:
			return "INSTALLED";
		case 0x4:
			return "RESOLVED";
		case 0x8:
			return "STARTING";
		case 0x10:
			return "STOPPING";
		case 0x1:
			return "UNINSTALLED";
		}
		throw new RuntimeException("not reachable");
	}

	// can be null if bundle doesn't have "Bnd-LastModified" property in bundle
	// manifest.
	public Date getBuildTimestamp() {
		return buildTimestamp;
	}

	public void setBuildTimestamp(Date l) {
		this.buildTimestamp = l;
	}

	@Override
	public String toString() {
		return String.format("%s [%s]", symbolicName, toStateName(state));
	}
}
