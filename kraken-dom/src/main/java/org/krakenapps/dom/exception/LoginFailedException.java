package org.krakenapps.dom.exception;

import org.krakenapps.msgbus.MsgbusException;

public class LoginFailedException extends MsgbusException {
	private static final long serialVersionUID = 1L;

	public LoginFailedException(String message) {
		super("dom", message);
	}
}
