package org.krakenapps.dom.exception;

import org.krakenapps.msgbus.MsgbusException;

public class DuplicateLoginNameException extends MsgbusException {
	private static final long serialVersionUID = 1L;

	public DuplicateLoginNameException() {
		super("dom", "duplicate-login-name");
	}
}
