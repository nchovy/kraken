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

public class TableProperties extends AbstractWordElement {

	@Override
	public String getTagName() {
		return "w:tblPr";
	}

	@Override
	public List<String> getChildElements() {
		return Arrays.asList("w:bidiVisual", "w:jc", "w:shd", "w:tblBorders", "w:tblCaption", "w:tblCellMar",
				"w:tblCellSpacing", "w:tblDescription", "w:tblInd", "w:tblLayout", "w:tblLook", "w:tblOverlap",
				"w:tblpPr", "w:tblStyle", "w:tblStyleColBandSize", "w:tblStyleRowBandSize", "w:tblW");
	}

}
