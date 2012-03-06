package org.krakenapps.word.model;

import java.util.Arrays;
import java.util.List;

public class TableGrid extends AbstractWordElement {

	@Override
	public String getTagName() {
		return "w:tblGrid";
	}

	@Override
	public List<String> getChildElements() {
		return Arrays.asList("w:gridCol", "w:tblGridChange");
	}
}
