package org.krakenapps.pkg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.krakenapps.api.PackageRepository;

public class PackageList {
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
