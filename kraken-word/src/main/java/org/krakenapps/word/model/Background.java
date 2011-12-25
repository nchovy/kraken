package org.krakenapps.word.model;

import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Background extends AbstractWordElement {
	private HexColor color;
	private String themeColor;

	@Override
	public String getTagName() {
		return "w:background";
	}

	@Override
	public List<String> getChildElements() {
		return Arrays.asList("w:drawing");
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

	@Override
	public Element toXml(Document d) {
		Element e = d.createElement("w:background");
		if (color != null)
			e.setAttribute("w:color", color.toString());
		if (themeColor != null)
			e.setAttribute("w:themeColor", themeColor);
		return e;
	}

	@Override
	public String toString() {
		return "Background [color=" + color + ", themeColor=" + themeColor + "]";
	}
}
