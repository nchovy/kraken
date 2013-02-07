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

public class Run extends AbstractWordElement {
	private static final List<String> CHILD_ELEMENTS = Arrays.asList("w:annotationRef", "w:br", "w:commentReference",
			"w:contentPart", "w:continuationSeparator", "w:cr", "w:dayLong", "w:dayShort", "w:delInstrText",
			"w:delText", "w:drawing", "w:endnoteRef", "w:endnoteReference", "w:endnoteReference", "w:fldChar",
			"w:footnoteRef", "w:footnoteReference", "w:instrText", "w:lastRenderedPageBreak", "w:monthLong",
			"w:monthShort", "w:noBreakHyphen", "w:object", "w:pgNum", "w:ptab", "w:rPr", "w:ruby", "w:separator",
			"w:softHyphen", "w:sym", "w:t", "w:tab", "w:yearLong", "w:yearShort");

	public Run() {
	}

	@Override
	public List<String> getChildElements() {
		return CHILD_ELEMENTS;
	}

	@Override
	public String getTagName() {
		return "w:r";
	}

}
