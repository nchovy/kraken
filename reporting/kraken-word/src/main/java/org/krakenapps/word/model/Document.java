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

import org.w3c.dom.Element;

public class Document extends AbstractWordElement {

	private Background background;
	private Body body;

	@Override
	public String getTagName() {
		return "w:document";
	}

	@Override
	public List<String> getChildElements() {
		return Arrays.asList("w:background", "w:body");
	}

	public Background getBackground() {
		return background;
	}

	public void setBackground(Background background) {
		this.background = background;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	@Override
	public Element toXml(org.w3c.dom.Document d) {
		Element g = d.createElementNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w:document");
		g.setAttribute("xmlns:wpc", "http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas");
		g.setAttribute("xmlns:mc", "http://schemas.openxmlformats.org/markup-compatibility/2006");
		g.setAttribute("xmlns:o", "urn:schemas-microsoft-com:office:office");
		g.setAttribute("xmlns:r", "http://schemas.openxmlformats.org/officeDocument/2006/relationships");
		g.setAttribute("xmlns:m", "http://schemas.openxmlformats.org/officeDocument/2006/math");
		g.setAttribute("xmlns:v", "urn:schemas-microsoft-com:vml");
		g.setAttribute("xmlns:wp14", "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing");
		g.setAttribute("xmlns:wp", "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing");
		g.setAttribute("xmlns:w10", "urn:schemas-microsoft-com:office:word");
		g.setAttribute("xmlns:w14", "http://schemas.microsoft.com/office/word/2010/wordml");
		g.setAttribute("xmlns:wpg", "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup");
		g.setAttribute("xmlns:wpi", "http://schemas.microsoft.com/office/word/2010/wordprocessingInk");
		g.setAttribute("xmlns:wne", "http://schemas.microsoft.com/office/word/2006/wordml");
		g.setAttribute("xmlns:wps", "http://schemas.microsoft.com/office/word/2010/wordprocessingShape");
		g.setAttribute("mc:Ignorable", "w14 wp14");

		if (background != null)
			g.appendChild(background.toXml(d));

		g.appendChild(body.toXml(d));

		return g;
	}
}
