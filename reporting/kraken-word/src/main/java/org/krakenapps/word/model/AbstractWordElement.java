package org.krakenapps.word.model;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AbstractWordElement implements WordElement {
	protected List<WordElement> children = new LinkedList<WordElement>();

	@Override
	public void add(WordElement el) {
		if (!getChildElements().contains(el.getTagName()))
			throw new SyntaxException(el.getTagName());

		children.add(el);
	}

	@Override
	public Element toXml(Document d) {
		Element e = d.createElement(getTagName());
		for (WordElement el : children)
			e.appendChild(el.toXml(d));
		return e;
	}
}
