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
 package org.krakenapps.sonar.passive.fingerprint;

public class HttpApplicationMetaData {
	private String vendor;
	private String name;
	private String version;

	public HttpApplicationMetaData(String vendor, String name, String version) {
		this.vendor = vendor;
		this.name = name;
		this.version = version;
	}

	public String getVendor() {
		return vendor;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public String toString() {
		return String.format("vendor=%s, name=%s, version=%s", vendor, name, version);
	}
}
