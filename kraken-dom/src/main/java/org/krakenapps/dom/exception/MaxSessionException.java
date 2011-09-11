package org.krakenapps.dom.exception;

import org.krakenapps.msgbus.Session;

public class MaxSessionException extends LoginFailedException {
	private static final long serialVersionUID = 1L;

	private Integer adminId;
	private Session session;

	public MaxSessionException() {
		this(null, null);
	}

	public MaxSessionException(Integer adminId, Session session) {
		super("max-session");
		this.adminId = adminId;
		this.session = session;
	}

	public Integer getAdminId() {
		return adminId;
	}

	public Session getSession() {
		return session;
	}
}
