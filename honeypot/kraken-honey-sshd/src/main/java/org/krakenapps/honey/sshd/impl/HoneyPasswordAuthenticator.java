package org.krakenapps.honey.sshd.impl;

import java.net.InetSocketAddress;

import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.krakenapps.honey.sshd.HoneyLoginAttemptListener;
import org.krakenapps.honey.sshd.HoneySshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoneyPasswordAuthenticator implements PasswordAuthenticator {
	private final Logger logger = LoggerFactory.getLogger(HoneyPasswordAuthenticator.class.getName());

	private HoneySshService sshd;

	public HoneyPasswordAuthenticator(HoneySshService sshd) {
		this.sshd = sshd;
	}

	@Override
	public boolean authenticate(String username, String password, ServerSession session) {
		logger.info("kraken honey sshd: login [{}] password [{}]", username, password);
		InetSocketAddress remote = (InetSocketAddress) session.getIoSession().getRemoteAddress();

		for (HoneyLoginAttemptListener listener : sshd.getLoginAttemptListeners()) {
			try {
				logger.debug("kraken honey sshd: login attempt [remote={}, username={}, password={}]", new Object[] {
						remote, username, password });
				listener.onLoginAttempt(remote, username, password);
			} catch (Throwable t) {
				logger.warn("kraken honey sshd: login attempt listener should not throw any exception", t);
			}
		}

		return password.equals("123456");
	}

}
