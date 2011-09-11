package org.krakenapps.dom.exception;

import org.krakenapps.msgbus.Session;

public class MaxSessionException extends LoginFailedException {
	private static final long serialVersionUID = 1L;

	private String loginName;
	private Session session;

	public MaxSessionException() {
		this(null, null);
	}

	public MaxSessionException(String loginName, Session session) {
		super("max-session");
		this.loginName = loginName;
		this.session = session;
	}

	public String getLoginName() {
		return loginName;
	}

	public Session getSession() {
		return session;
	}
}
