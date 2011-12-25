package org.krakenapps.word.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TableTopBorder extends AbstractWordElement {

	private String value;
	private int size;
	private int space;
	private HexColor color;
	private String themeColor;

	public TableTopBorder() {
	}

	public TableTopBorder(String value, int size, int space, HexColor color) {
		this.value = value;
		this.size = size;
		this.space = space;
		this.color = color;
	}

	@Override
	public String getTagName() {
		return "w:top";
	}

	@Override
	public List<String> getChildElements() {
		return new ArrayList<String>();
	}

	@Override
	public Element toXml(Document d) {
		Element el = d.createElement(getTagName());
		el.setAttribute("w:val", value);
		el.setAttribute("w:sz", Integer.toString(size));
		el.setAttribute("w:space", Integer.toString(space));
		el.setAttribute("w:color", color.toString());
		if (themeColor != null)
			el.setAttribute("w:themeColor", themeColor);
		return el;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSpace() {
		return space;
	}

	public void setSpace(int space) {
		this.space = space;
	}

	public HexColor getColor() {
		return color;
	}

	public void setColor(HexColor color) {
		this.color = color;
	}

	public String getThemeColor() {
		return themeColor;
	}

	public void setThemeColor(String themeColor) {
		this.themeColor = themeColor;
	}

}
