package org.krakenapps.word.model;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ParagraphStyle extends AbstractWordElement {
	private String value;

	public ParagraphStyle() {
	}

	public ParagraphStyle(String value) {
		this.value = value;
	}

	@Override
	public String getTagName() {
		return "w:pStyle";
	}

	@Override
	public List<String> getChildElements() {
		return new ArrayList<String>();
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public Element toXml(Document d) {
		Element el = d.createElement(getTagName());
		el.setAttribute("w:val", value);
		return el;
	}
}
