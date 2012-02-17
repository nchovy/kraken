package org.krakenapps.dom.model.windows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WindowsHost {
	private String computerName;

	private String workGroup;

	private String windowsVersion;

	private String servicePackVersion;

	// hardening category enum name to value mappings
	private Map<String, Object> hardenings = new HashMap<String, Object>();

	// softwares
	private List<WindowsSoftware> softwares = new ArrayList<WindowsSoftware>();

	// identity to update mappings
	private Map<String, WindowsUpdate> updates = new HashMap<String, WindowsUpdate>();

	public String getComputerName() {
		return computerName;
	}

	public void setComputerName(String computerName) {
		this.computerName = computerName;
	}

	public String getWorkGroup() {
		return workGroup;
	}

	public void setWorkGroup(String workGroup) {
		this.workGroup = workGroup;
	}

	public String getWindowsVersion() {
		return windowsVersion;
	}

	public void setWindowsVersion(String windowsVersion) {
		this.windowsVersion = windowsVersion;
	}

	public String getServicePackVersion() {
		return servicePackVersion;
	}

	public void setServicePackVersion(String servicePackVersion) {
		this.servicePackVersion = servicePackVersion;
	}

	public Map<String, Object> getHardenings() {
		return hardenings;
	}

	public void setHardenings(Map<String, Object> hardenings) {
		this.hardenings = hardenings;
	}

	public List<WindowsSoftware> getSoftwares() {
		return softwares;
	}

	public void setSoftwares(List<WindowsSoftware> softwares) {
		this.softwares = softwares;
	}

	public Map<String, WindowsUpdate> getUpdates() {
		return updates;
	}

	public void setUpdates(Map<String, WindowsUpdate> updates) {
		this.updates = updates;
	}

}
