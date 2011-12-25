package org.krakenapps.word.model;

import java.util.Arrays;
import java.util.List;

public class Table extends AbstractWordElement {

	private static final List<String> CHILD_ELEMENTS = Arrays.asList("w:bookmarkEnd", "w:bookmarkStart",
			"w:commentRangeEnd", "w:commentRangeStart", "w:customXml", "w:customXmlDelRangeEnd",
			"w:customXmlDelRangeStart", "w:customXmlInsRangeEnd", "w:customXmlInsRangeStart",
			"w:customXmlMoveFromRangeEnd", "w:customXmlMoveFromRangeStart", "w:customXmlMoveToRangeEnd",
			"w:customXmlMoveToRangeStart", "w:del", "w:ins", "w:moveFrom", "w:moveFromRangeEnd", "w:moveFromRangeEnd",
			"w:moveFromRangeStart", "w:moveTo", "w:moveToRangeEnd", "w:moveToRangeStart", "w:oMath", "w:oMathPara",
			"w:permEnd", "w:permStart", "w:proofErr", "w:sdt", "w:tblGrid", "w:tblPr", "w:tr");

	@Override
	public String getTagName() {
		return "w:tbl";
	}

	@Override
	public List<String> getChildElements() {
		return CHILD_ELEMENTS;
	}

}
