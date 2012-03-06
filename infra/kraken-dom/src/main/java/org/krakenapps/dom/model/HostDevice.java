package org.krakenapps.dom.model;

import java.util.Map;

public class HostDevice {
	private HostDeviceType type;

	private String name;

	private String vendor;

	private boolean enabled;

	// device type specific additional properties
	private Map<String, Object> extentions;

	public HostDeviceType getType() {
		return type;
	}

	public void setType(HostDeviceType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Map<String, Object> getExtentions() {
		return extentions;
	}

	public void setExtentions(Map<String, Object> extentions) {
		this.extentions = extentions;
	}
}
