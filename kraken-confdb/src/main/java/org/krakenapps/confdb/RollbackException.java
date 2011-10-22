package org.krakenapps.confdb;

public class RollbackException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private Throwable cause;

	public RollbackException(Throwable cause) {
		this.cause = cause;
	}

	public Throwable getCause() {
		return cause;
	}

	@Override
	public String getMessage() {
		return cause.getMessage();
	}

}
