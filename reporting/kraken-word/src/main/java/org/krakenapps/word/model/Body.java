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

public class Body extends AbstractWordElement {

	@Override
	public String getTagName() {
		return "w:body";
	}

	@Override
	public List<String> getChildElements() {
		return Arrays.asList("w:altChunk", "w:bookmarkEnd", "w:bookmarkStart", "w:commentRangeEnd",
				"w:commentRangeStart", "w:customXml", "w:customXmlDelRangeEnd", "w:customXmlDelRangeStart",
				"w:customXmlInsRangeEnd", "w:customXmlInsRangeStart", "w:customXmlMoveFromRangeEnd",
				"w:customXmlMoveToRangeStart", "w:del", "w:ins", "w:moveFrom", "w:moveFromRangeEnd",
				"w:moveFromRangeStart", "w:moveTo", "w:moveToRangeEnd", "w:moveToRangeStart", "w:oMath", "w:oMathPara",
				"w:p", "w:permEnd", "w:permStart", "w:proofErr", "w:std", "w:sectPr", "w:tbl");
	}

}
