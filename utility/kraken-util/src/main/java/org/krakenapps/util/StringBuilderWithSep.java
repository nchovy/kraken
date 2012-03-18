package org.krakenapps.util;

public class StringBuilderWithSep {
	private StringBuilder sb = new StringBuilder();
	private String separater;

	public StringBuilderWithSep(String sep) {
		this.separater = sep;
	}

	public StringBuilderWithSep append(Object s) {
		if (sb.length() != 0) {
			sb.append(separater);
		}
		sb.append(s.toString());
		return this;
	}

	public StringBuilderWithSep append(Object... args) {
		for (Object s : args) {
			append(s);
		}
		return this;
	}

	public String toString() {
		return sb.toString();
	}
}
