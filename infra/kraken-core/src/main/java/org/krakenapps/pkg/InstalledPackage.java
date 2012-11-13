package org.krakenapps.pkg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.krakenapps.api.CollectionTypeHint;
import org.krakenapps.api.FieldOption;
import org.krakenapps.confdb.CollectionName;

@CollectionName("installed_pkgs")
public class InstalledPackage {
	@FieldOption(nullable = false)
	private String name;

	@FieldOption(nullable = false)
	private String version;

	@FieldOption(nullable = false)
	private Date released;

	@FieldOption(nullable = true)
	private String description;

	@CollectionTypeHint(PackageBundleRequirement.class)
	private List<PackageBundleRequirement> bundleRequirements = new ArrayList<PackageBundleRequirement>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Date getReleased() {
		return released;
	}

	public void setReleased(Date released) {
		this.released = released;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<PackageBundleRequirement> getBundleRequirements() {
		return bundleRequirements;
	}

	public void setBundleRequirements(List<PackageBundleRequirement> bundleRequirements) {
		this.bundleRequirements = bundleRequirements;
	}
}
