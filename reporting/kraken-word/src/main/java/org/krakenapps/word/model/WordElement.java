package org.krakenapps.word.model;

import java.util.List;

import org.w3c.dom.Element;

public interface WordElement {
	String getTagName();

	List<String> getChildElements();

	void add(WordElement el);

	Element toXml(org.w3c.dom.Document d);
}
