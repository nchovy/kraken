package org.krakenapps.dom.api;

import java.util.Map;

import org.krakenapps.msgbus.MsgbusException;

public class DOMException extends MsgbusException {
	private static final long serialVersionUID = 1L;

	public DOMException(String errorCode) {
		this(errorCode, null);
	}

	public DOMException(String errorCode, Map<String, Object> parameters) {
		super("dom", errorCode, parameters);
	}
}
