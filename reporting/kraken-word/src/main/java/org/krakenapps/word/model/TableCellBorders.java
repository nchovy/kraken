package org.krakenapps.word.model;

import java.util.Arrays;
import java.util.List;

public class TableCellBorders extends AbstractWordElement {

	@Override
	public String getTagName() {
		return "w:tcBorders";
	}

	@Override
	public List<String> getChildElements() {
		return Arrays.asList("w:bottom", "w:end", "w:insideH", "w:insideV", "w:start", "w:tl2br", "w:top", "w:tr2bl",
				"w:left", "w:right");
	}
}
