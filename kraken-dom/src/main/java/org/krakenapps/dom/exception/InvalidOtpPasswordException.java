package org.krakenapps.dom.exception;

public class InvalidOtpPasswordException extends LoginFailedException {
	private static final long serialVersionUID = 1L;

	public InvalidOtpPasswordException() {
		super("invalid-otp-password");
	}
}
