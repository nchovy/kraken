package org.krakenapps.word.model;

import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Text extends AbstractWordElement {

	private String content;

	public Text() {
	}

	public Text(String content) {
		this.content = content;
	}

	@Override
	public String getTagName() {
		return "w:t";
	}

	@Override
	public List<String> getChildElements() {
		return Arrays.asList();
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public Element toXml(Document d) {
		Element el = d.createElement(getTagName());
		el.setTextContent(content);
		return el;
	}
}
