package org.krakenapps.word.model;

public class HexColor {
	private String value;

	public HexColor() {
	}

	public HexColor(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
