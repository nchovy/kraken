package org.krakenapps.word.model;

//see $22.9.2.14
public class TwipsMeasure {
	private int value;

	public TwipsMeasure() {
	}

	public TwipsMeasure(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}

}
