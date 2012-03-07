package org.krakenapps.word.model;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GridColumnDefinition extends AbstractWordElement {
	private TwipsMeasure width;

	public GridColumnDefinition() {
	}

	public GridColumnDefinition(TwipsMeasure width) {
		this.width = width;
	}

	@Override
	public String getTagName() {
		return "w:gridCol";
	}

	@Override
	public List<String> getChildElements() {
		return new ArrayList<String>();
	}

	@Override
	public Element toXml(Document d) {
		Element el = d.createElement(getTagName());
		el.setAttribute("w:w", width.toString());
		return el;
	}
}
