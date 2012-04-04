package org.krakenapps.honey.sshd;

public abstract class HoneyBaseCommandHandler implements HoneyCommandHandler {
	private HoneySshSession session;

	public HoneySshSession getSession() {
		return session;
	}

	public void setSession(HoneySshSession session) {
		this.session = session;
	}
}
