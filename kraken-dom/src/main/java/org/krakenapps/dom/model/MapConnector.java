package org.krakenapps.dom.model;

public class MapConnector {
	private String type;
	private String from;
	private String to;
	private String color;
	private int thickness;
	private boolean useThreshold;
	private String thresholdPath;
	private Long threshold;
	private String onClick;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getThickness() {
		return thickness;
	}

	public void setThickness(int thickness) {
		this.thickness = thickness;
	}

	public boolean isUseThreshold() {
		return useThreshold;
	}

	public void setUseThreshold(boolean useThreshold) {
		this.useThreshold = useThreshold;
	}

	public String getThresholdPath() {
		return thresholdPath;
	}

	public void setThresholdPath(String thresholdPath) {
		this.thresholdPath = thresholdPath;
	}

	public Long getThreshold() {
		return threshold;
	}

	public void setThreshold(Long threshold) {
		this.threshold = threshold;
	}

	public String getOnClick() {
		return onClick;
	}

	public void setOnClick(String onClick) {
		this.onClick = onClick;
	}
}
