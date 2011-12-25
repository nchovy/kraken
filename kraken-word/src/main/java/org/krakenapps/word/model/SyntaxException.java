package org.krakenapps.word.model;

public class SyntaxException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String msg;

	public SyntaxException(String msg) {
		this.msg = msg;
	}

	@Override
	public String getMessage() {
		return msg;
	}
}
