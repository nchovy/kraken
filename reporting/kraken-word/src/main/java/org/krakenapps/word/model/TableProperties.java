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
