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

public class TableCellProperties extends AbstractWordElement {

	private static final List<String> CHILD_ELEMENTS = Arrays.asList("w:cellDel", "w:cellIns", "w:cellMerge",
			"w:cnfStyle", "w:gridSpan", "w:headers", "w:hideMark", "w:hMerge", "w:noWrap", "w:shd", "w:tcBorders",
			"w:tcFitText", "w:tcMar", "w:tcPrChange", "w:tcW", "w:textDirection", "w:vAlign", "w:vMerge");

	@Override
	public String getTagName() {
		return "w:tcPr";
	}

	@Override
	public List<String> getChildElements() {
		return CHILD_ELEMENTS;
	}

}
