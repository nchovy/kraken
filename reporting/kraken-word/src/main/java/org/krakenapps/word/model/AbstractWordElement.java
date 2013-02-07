/*
 * Copyright 2012 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
