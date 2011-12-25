package org.krakenapps.word.model;

import java.util.Arrays;
import java.util.List;

public class ParagraphProperties extends AbstractWordElement {

	@Override
	public String getTagName() {
		return "w:pPr";
	}

	@Override
	public List<String> getChildElements() {
		// TODO:
		return Arrays.asList("w:pStyle");
	}

}
