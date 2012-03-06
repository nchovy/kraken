package org.krakenapps.dom.model.windows;

import java.util.Date;

public class WindowsSoftware {
	// see registry key DisplayName
	private String displayName;

	// see registry key DisplayVersion
	private String displayVersion;

	// see registry key Publisher
	private String publisher;

	// see registry key URLInfoAbout
	private String urlInfoAbout;

	// see registry key InstallLocation
	private String installLocation;

	// see registry key InstallDate
	private Date installDate;

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayVersion() {
		return displayVersion;
	}

	public void setDisplayVersion(String displayVersion) {
		this.displayVersion = displayVersion;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getUrlInfoAbout() {
		return urlInfoAbout;
	}

	public void setUrlInfoAbout(String urlInfoAbout) {
		this.urlInfoAbout = urlInfoAbout;
	}

	public String getInstallLocation() {
		return installLocation;
	}

	public void setInstallLocation(String installLocation) {
		this.installLocation = installLocation;
	}

	public Date getInstallDate() {
		return installDate;
	}

	public void setInstallDate(Date installDate) {
		this.installDate = installDate;
	}
}
