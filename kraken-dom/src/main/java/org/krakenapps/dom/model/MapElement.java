package org.krakenapps.dom.model;

import java.util.UUID;

import org.krakenapps.api.FieldOption;

public class MapElement {
	public static enum Type {
		Image, Label
	};

	@FieldOption(nullable = false)
	private Type type;

	// Common Properties
	@FieldOption(nullable = false)
	private String guid = UUID.randomUUID().toString();
	private Double x;
	private Double y;
	private Integer z;
	private Double width;
	private Double height;
	private String dataBinding;
	private String onClick;

	// Map Image Properties
	private String path;
	private Boolean useThreshold;
	private Long threshold;
	private String thresholdPath;

	// Map Label Properties
	@FieldOption(length = 10)
	private String fontColor;
	private String text;
	private Double fontSize;
	private String align;
	private String borderColor;
	private String backgroundColor;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public Integer getZ() {
		return z;
	}

	public void setZ(Integer z) {
		this.z = z;
	}

	public Double getWidth() {
		return width;
	}

	public void setWidth(Double width) {
		this.width = width;
	}

	public Double getHeight() {
		return height;
	}

	public void setHeight(Double height) {
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

	public Boolean getUseThreshold() {
		return useThreshold;
	}

	public void setUseThreshold(Boolean useThreshold) {
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

	public Double getFontSize() {
		return fontSize;
	}

	public void setFontSize(Double fontSize) {
		this.fontSize = fontSize;
	}

	public String getAlign() {
		return align;
	}

	public void setAlign(String align) {
		this.align = align;
	}

	public String getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
}
