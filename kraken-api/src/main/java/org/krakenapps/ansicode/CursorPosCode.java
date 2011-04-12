package org.krakenapps.ansicode;

public class CursorPosCode extends AnsiEscapeCode {
	public enum Option {
		Save, Restore
	}
	
	private Option option;

	public CursorPosCode(Option op) {
		this.option = op;
	}

	private String getOptionCode() {
		switch (option) {
		case Save:
			return "s";
		case Restore:
			return "u";
		}

		throw new RuntimeException("Invalid option code. not reachable");
	}

	@Override
	public byte[] toByteArray() {
		return wrapCSI(getOptionCode());
	}
}
