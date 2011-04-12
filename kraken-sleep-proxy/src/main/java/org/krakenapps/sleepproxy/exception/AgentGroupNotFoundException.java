package org.krakenapps.sleepproxy.exception;

public class AgentGroupNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private int id;

	public AgentGroupNotFoundException(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
