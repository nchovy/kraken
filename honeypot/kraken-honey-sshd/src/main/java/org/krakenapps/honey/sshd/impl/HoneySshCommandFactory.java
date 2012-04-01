package org.krakenapps.honey.sshd.impl;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.krakenapps.honey.sshd.HoneySshService;

public class HoneySshCommandFactory implements Factory<Command> {
	private HoneySshService sshd;

	public HoneySshCommandFactory(HoneySshService sshd) {
		this.sshd = sshd;
	}

	@Override
	public Command create() {
		HoneySshSessionImpl session = new HoneySshSessionImpl(sshd);
		session.addListener(new HoneySshHandler(session));
		return session;
	}
}
