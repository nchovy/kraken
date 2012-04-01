package org.krakenapps.honey.sshd.impl;

import org.krakenapps.honey.sshd.HoneySshSession;
import org.krakenapps.honey.sshd.handler.Shell;
import org.krakenapps.termio.TerminalEventListener;
import org.krakenapps.termio.TerminalSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoneySshHandler implements TerminalEventListener {
	private final Logger logger = LoggerFactory.getLogger(HoneySshHandler.class.getName());
	private HoneySshSession session;
	private Shell shell;

	public HoneySshHandler(HoneySshSession session) {
		this.session = session;
	}

	@Override
	public void onConnected(TerminalSession session) {
		shell = new Shell();
		shell.setSession(this.session);
		shell.start();
	}

	@Override
	public void onDisconnected(TerminalSession session) {
		shell.kill();
	}

	@Override
	public void onCommand(TerminalSession session, int command, byte[] option) {
	}

	@Override
	public void onData(TerminalSession session, byte b) {
		try {
			session.getInputStream().push(b);
		} catch (InterruptedException e) {
		}
	}

}
