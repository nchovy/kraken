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
