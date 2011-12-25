package org.krakenapps.word.model;

import java.util.Arrays;
import java.util.List;

public class TableRow extends AbstractWordElement {

	private static final List<String> CHILD_ELEMENTS = Arrays.asList("w:bookmarkEnd", "w:bookmarkStart",
			"w:commentRangeEnd", "w:commentRangeStart", "w:customXml", "w:customXmlDelRangeEnd",
			"w:customXmlDelRangeStart", "w:customXmlInsRangeEnd", "w:customXmlInsRangeStart",
			"w:customXmlMoveFromRangeEnd", "w:customXmlMoveFromRangeStart", "w:customXmlMoveToRangeEnd",
			"w:customXmlMoveToRangeStart", "w:del", "w:ins", "w:moveFrom", "w:moveFromRangeEnd", "w:moveFromRangeEnd",
			"w:moveFromRangeStart", "w:moveTo", "w:moveToRangeEnd", "w:moveToRangeStart", "w:oMath", "w:oMathPara",
			"w:permEnd", "w:permStart", "w:proofErr", "w:sdt", "w:tblPrEx", "w:tc", "w:trPr");

	private LongHexNumber rsidDel;
	private LongHexNumber rsidR;
	private LongHexNumber rsidRPr;
	private LongHexNumber rsidTr;

	@Override
	public String getTagName() {
		return "w:tr";
	}

	@Override
	public List<String> getChildElements() {
		return CHILD_ELEMENTS;
	}

	public LongHexNumber getRsidDel() {
		return rsidDel;
	}

	public void setRsidDel(LongHexNumber rsidDel) {
		this.rsidDel = rsidDel;
	}

	public LongHexNumber getRsidR() {
		return rsidR;
	}

	public void setRsidR(LongHexNumber rsidR) {
		this.rsidR = rsidR;
	}

	public LongHexNumber getRsidRPr() {
		return rsidRPr;
	}

	public void setRsidRPr(LongHexNumber rsidRPr) {
		this.rsidRPr = rsidRPr;
	}

	public LongHexNumber getRsidTr() {
		return rsidTr;
	}

	public void setRsidTr(LongHexNumber rsidTr) {
		this.rsidTr = rsidTr;
	}

}
