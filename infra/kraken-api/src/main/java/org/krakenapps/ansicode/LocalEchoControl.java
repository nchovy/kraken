package org.krakenapps.ansicode;
public class LocalEchoControl extends AnsiEscapeCode {
	public enum Option {
		Set, Reset
	}

	private Option option;

	public LocalEchoControl(Option option) {
		this.option = option;
	}

	@Override
	public byte[] toByteArray() {
		switch (option) {
		case Set:
			return wrapCSI("12h");
		case Reset:
			return wrapCSI("12l");
		}
		throw new RuntimeException(
				"Invalid clear screen option. not reachable.");
	}
}
