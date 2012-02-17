package org.krakenapps.dom.model.windows;

import java.util.List;

public class WindowsUpdate {
	private String identity;

	// localized title
	private String title;

	// localized description
	private String description;

	private List<String> bulletins;

	private List<String> supersededUpdates;

	// e.g. KB971029
	private List<String> kbArticles;

	private boolean installed;

	private boolean downloaded;

	private boolean hidden;

	private boolean mandatory;

	private int msrcSeverity;

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

	public List<String> getBulletins() {
		return bulletins;
	}

	public void setBulletins(List<String> bulletins) {
		this.bulletins = bulletins;
	}

	public List<String> getSupersededUpdates() {
		return supersededUpdates;
	}

	public void setSupersededUpdates(List<String> supersededUpdates) {
		this.supersededUpdates = supersededUpdates;
	}

	public List<String> getKbArticles() {
		return kbArticles;
	}

	public void setKbArticles(List<String> kbArticles) {
		this.kbArticles = kbArticles;
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

	public int getMsrcSeverity() {
		return msrcSeverity;
	}

	public void setMsrcSeverity(int msrcSeverity) {
		this.msrcSeverity = msrcSeverity;
	}
}
