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

public class TableInsideVerticalEdgesBorder extends AbstractWordElement {
	private String value;
	private int size;
	private int space;
	private HexColor color;
	private String themeColor;

	public TableInsideVerticalEdgesBorder() {
	}

	public TableInsideVerticalEdgesBorder(String value, int size, int space, HexColor color) {
		this.value = value;
		this.size = size;
		this.space = space;
		this.color = color;
	}

	@Override
	public String getTagName() {
		return "w:insideV";
	}

	@Override
	public List<String> getChildElements() {
		return new ArrayList<String>();
	}

	@Override
	public Element toXml(Document d) {
		Element el = d.createElement(getTagName());
		el.setAttribute("w:val", value);
		el.setAttribute("w:sz", Integer.toString(size));
		el.setAttribute("w:space", Integer.toString(space));
		el.setAttribute("w:color", color.toString());
		if (themeColor != null)
			el.setAttribute("w:themeColor", themeColor);
		return el;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSpace() {
		return space;
	}

	public void setSpace(int space) {
		this.space = space;
	}

	public HexColor getColor() {
		return color;
	}

	public void setColor(HexColor color) {
		this.color = color;
	}

	public String getThemeColor() {
		return themeColor;
	}

	public void setThemeColor(String themeColor) {
		this.themeColor = themeColor;
	}

}
