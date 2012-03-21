package org.krakenapps.dom.model.windows;

public class WindowsUpdate {
	private String identity;

	// localized title
	private String title;

	// localized description
	private String description;

	private boolean installed;

	private boolean downloaded;

	private boolean hidden;

	private boolean mandatory;

	private String msrcSeverity;

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isInstalled() {
		return installed;
	}

	public void setInstalled(boolean installed) {
		this.installed = installed;
	}

	public boolean isDownloaded() {
		return downloaded;
	}

	public void setDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public String getMsrcSeverity() {
		return msrcSeverity;
	}

	public void setMsrcSeverity(String msrcSeverity) {
		this.msrcSeverity = msrcSeverity;
	}
}
