package org.krakenapps.honey.sshd.impl;

import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoneyPasswordAuthenticator implements PasswordAuthenticator {
	private final Logger logger = LoggerFactory.getLogger(HoneyPasswordAuthenticator.class.getName());

	@Override
	public boolean authenticate(String username, String password, ServerSession session) {
		logger.info("kraken honey sshd: login [{}] password [{}]", username, password);
		return true;
	}

}
