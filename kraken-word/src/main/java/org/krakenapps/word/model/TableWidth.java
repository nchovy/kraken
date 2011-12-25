package org.krakenapps.word.model;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TableWidth extends AbstractWordElement {

	private String type;
	private int width;

	public TableWidth() {
	}

	public TableWidth(String type, int width) {
		this.type = type;
		this.width = width;
	}

	@Override
	public String getTagName() {
		return "w:tblW";
	}

	@Override
	public List<String> getChildElements() {
		return new ArrayList<String>();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public Element toXml(Document d) {
		if (type == null)
			throw new RuntimeException("table width requiers type");

		Element el = d.createElement(getTagName());
		el.setAttribute("w:type", type);
		el.setAttribute("w:w", Integer.toString(width));
		return el;
	}
}
