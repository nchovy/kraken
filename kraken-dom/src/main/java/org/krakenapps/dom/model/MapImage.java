package org.krakenapps.dom.model;

public class MapImage extends MapElement {
	/**
	 * initial or unknown state image path
	 */
	private String path;
	private boolean useThreshold;
	private Long threshold;
	private String thresholdPath;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isUseThreshold() {
		return useThreshold;
	}

	public void setUseThreshold(boolean useThreshold) {
		this.useThreshold = useThreshold;
	}

	public Long getThreshold() {
		return threshold;
	}

	public void setThreshold(Long threshold) {
		this.threshold = threshold;
	}

	public String getThresholdPath() {
		return thresholdPath;
	}

	public void setThresholdPath(String thresholdPath) {
		this.thresholdPath = thresholdPath;
	}
}
