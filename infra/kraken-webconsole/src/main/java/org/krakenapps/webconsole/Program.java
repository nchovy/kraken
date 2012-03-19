/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.webconsole;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Program {
	private long bundleId;
	private String packageId;
	private String programId;
	private String path;
	private Map<Locale, String> labels;

	public Program(long bundleId, String packageId, String programId, String path) {
		this.bundleId = bundleId;
		this.packageId = packageId;
		this.programId = programId;
		this.path = path;
		this.labels = new HashMap<Locale, String>();
	}

	public long getBundleId() {
		return bundleId;
	}

	public String getPackageId() {
		return packageId;
	}

	public String getProgramId() {
		return programId;
	}

	public String getPath() {
		return path;
	}

	public Map<Locale, String> getLabels() {
		return labels;
	}

	@Override
	public String toString() {
		return "package=" + packageId + ", program=" + programId + ", path=" + path + ", bundle=" + bundleId;
	}
}
