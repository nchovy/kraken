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
