package org.krakenapps.dom.model;

public class MapElement {
	private double x;
	private double y;
	private int z;
	private double width;
	private double height;
	private String dataBinding;
	private String onClick;

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
}
