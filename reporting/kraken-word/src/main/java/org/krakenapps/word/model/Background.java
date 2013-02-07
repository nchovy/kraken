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

public class Background extends AbstractWordElement {
	private HexColor color;
	private String themeColor;

	@Override
	public String getTagName() {
		return "w:background";
	}

	@Override
	public List<String> getChildElements() {
		return Arrays.asList("w:drawing");
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

	@Override
	public Element toXml(Document d) {
		Element e = d.createElement("w:background");
		if (color != null)
			e.setAttribute("w:color", color.toString());
		if (themeColor != null)
			e.setAttribute("w:themeColor", themeColor);
		return e;
	}

	@Override
	public String toString() {
		return "Background [color=" + color + ", themeColor=" + themeColor + "]";
	}
}
