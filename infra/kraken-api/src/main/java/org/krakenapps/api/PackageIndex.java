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
package org.krakenapps.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.krakenapps.api.PackageRepository;

public class PackageIndex {
	private PackageRepository repository;
	private String description;
	private List<PackageMetadata> packages = new ArrayList<PackageMetadata>();
	private Date created;

	public PackageRepository getRepository() {
		return repository;
	}

	public void setRepository(PackageRepository repository) {
		this.repository = repository;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<PackageMetadata> getPackages() {
		return packages;
	}

	public void setPackages(List<PackageMetadata> packages) {
		this.packages = packages;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}
}
