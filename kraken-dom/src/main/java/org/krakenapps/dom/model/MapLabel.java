package org.krakenapps.dom.model;

import org.krakenapps.api.FieldOption;

public class MapLabel extends MapElement {
	@FieldOption(length = 10)
	private String fontColor;

	private String text;
	private int fontSize;
	private String align;

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
