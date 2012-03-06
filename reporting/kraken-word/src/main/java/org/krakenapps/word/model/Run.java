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
