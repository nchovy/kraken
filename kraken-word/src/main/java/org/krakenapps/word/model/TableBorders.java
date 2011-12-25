package org.krakenapps.word.model;

import java.util.Arrays;
import java.util.List;

public class TableBorders extends AbstractWordElement {

	private static final List<String> CHILD_ELEMENTS = Arrays.asList("w:bottom", "w:end", "w:insideH", "w:insideV",
			"w:start", "w:top");

	@Override
	public String getTagName() {
		return "w:tblBorders";
	}

	@Override
	public List<String> getChildElements() {
		return CHILD_ELEMENTS;
	}

}
