package org.krakenapps.dom.model;

import org.krakenapps.api.FieldOption;

public class MapElement {
	public static enum Type {
		Image, Label
	};

	@FieldOption(nullable = false)
	private Type type;

	// Common Properties
	private double x;
	private double y;
	private int z;
	private double width;
	private double height;
	private String dataBinding;
	private String onClick;

	// Map Image Properties
	private String path;
	private boolean useThreshold;
	private Long threshold;
	private String thresholdPath;

	// Map Label Properties
	@FieldOption(length = 10)
	private String fontColor;
	private String text;
	private int fontSize;
	private String align;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public String getDataBinding() {
		return dataBinding;
	}

	public void setDataBinding(String dataBinding) {
		this.dataBinding = dataBinding;
	}

	public String getOnClick() {
		return onClick;
	}

	public void setOnClick(String onClick) {
		this.onClick = onClick;
	}

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

	public String getFontColor() {
		return fontColor;
	}

	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public String getAlign() {
		return align;
	}

	public void setAlign(String align) {
		this.align = align;
	}
}
