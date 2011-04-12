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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PackageDescriptor {
	private String name;

	private Version version;

	private Date date;

	private String description;

	private List<BundleRequirement> bundles;

	private List<MavenArtifact> artifacts;

	private List<String> startBundleNames;

	public PackageDescriptor(String name, Version version, Date date) {
		this.name = name;
		this.version = version;
		this.date = date;
		this.bundles = new ArrayList<BundleRequirement>();
		this.artifacts = new ArrayList<MavenArtifact>();
		this.startBundleNames = new ArrayList<String>();
	}

	public PackageDescriptor(String name, Version version, Date date, String description) {
		this(name, version, date);
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public Version getVersion() {
		return version;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public List<BundleRequirement> getBundleRequirements() {
		return bundles;
	}

	public List<MavenArtifact> getMavenArtifacts() {
		return artifacts;
	}

	public List<String> getStartBundleNames() {
		return startBundleNames;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return String.format(
				"{name: %s, version: %s, date: %s, bundle count: %d, artifact count: %d, start bundles: %d}",
				name, version, dateFormat.format(date), bundles.size(), artifacts.size(), startBundleNames.size());
	}

}
