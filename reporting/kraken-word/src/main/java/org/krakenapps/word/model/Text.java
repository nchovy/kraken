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
