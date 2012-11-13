package org.krakenapps.pkg;

import org.krakenapps.api.FieldOption;

/**
 * embedded object for InstalledPackage. Represents bundle requirements of the
 * package
 * 
 * @author xeraph
 * 
 */
public class PackageBundleRequirement {
	@FieldOption(nullable = false)
	private String name;

	@FieldOption(nullable = false)
	private String lowVersion;

	@FieldOption(nullable = false)
	private String highVersion;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLowVersion() {
		return lowVersion;
	}

	public void setLowVersion(String lowVersion) {
		this.lowVersion = lowVersion;
	}

	public String getHighVersion() {
		return highVersion;
	}

	public void setHighVersion(String highVersion) {
		this.highVersion = highVersion;
	}

}
