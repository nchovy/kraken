package org.krakenapps.word.model;

import java.util.Arrays;
import java.util.List;

public class Paragraph extends AbstractWordElement {

	private static final List<String> CHILD_ELEMENTS = Arrays.asList("w:bdo", "w:bookmarkEnd", "w:bookmarkStart",
			"w:commentRangeEnd", "w:commentRangeStart", "w:customXml", "w:customXmlDelRangeEnd",
			"w:customXmlDelRangeStart", "w:customXmlMoveFromRangeEnd", "w:customXmlMoveFromRangeStart",
			"w:customXmlMoveToRangeEnd", "w:customXmlMoveToRangeStart", "w:del", "w:dir", "w:fldSimple", "w:hyperlink",
			"w:ins", "w:moveFrom", "w:moveFromRangeEnd", "w:moveFromRangeStart", "w:moveTo", "w:moveToRangeEnd",
			"w:moveToRangeStart", "w:oMath", "w:oMathPara", "w:permEnd", "w:permStart", "w:pPr", "w:proofErr", "w:r",
			"w:sdt", "w:smartTag", "w:subDoc");

	@Override
	public String getTagName() {
		return "w:p";
	}

	@Override
	public List<String> getChildElements() {
		return CHILD_ELEMENTS;
	}
}
