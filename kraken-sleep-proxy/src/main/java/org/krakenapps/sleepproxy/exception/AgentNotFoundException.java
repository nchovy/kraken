package org.krakenapps.sleepproxy.exception;

public class AgentNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private String guid;

	public AgentNotFoundException(String guid) {
		this.guid = guid;
	}

	public String getGuid() {
		return guid;
	}
}
