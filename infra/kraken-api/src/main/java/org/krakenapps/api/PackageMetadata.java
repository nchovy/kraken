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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.krakenapps.api.PackageRepository;
import org.krakenapps.api.PackageVersionHistory;

public class PackageMetadata {
	private PackageRepository repository;
	private String name;
	private String description;

	private List<PackageVersionHistory> versions;
	private Set<String> mavenRepositories;

	public PackageMetadata() {
		versions = new ArrayList<PackageVersionHistory>();
		mavenRepositories = new HashSet<String>();
	}

	public PackageRepository getRepository() {
		return repository;
	}

	public void setRepository(PackageRepository repository) {
		this.repository = repository;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<PackageVersionHistory> getVersions() {
		return versions;
	}

	public void setVersions(List<PackageVersionHistory> versions) {
		this.versions = versions;
	}

	public Set<String> getMavenRepositories() {
		return mavenRepositories;
	}

	public void setMavenRepositories(Set<String> mavenRepositories) {
		this.mavenRepositories = mavenRepositories;
	}

}
