package org.krakenapps.ansicode;

public class ScrollCode extends AnsiEscapeCode {
	private boolean up;
	private int lines;

	public ScrollCode(boolean up) {
		this(up, 1);
	}

	public ScrollCode(boolean up, int lines) {
		this.up = up;
		this.lines = lines;
	}

	@Override
	public byte[] toByteArray() {
		if (up)
			return wrapCSI(lines + "S");
		else
			return wrapCSI(lines + "T");
	}
}
