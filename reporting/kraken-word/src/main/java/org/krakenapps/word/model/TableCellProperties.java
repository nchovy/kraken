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
