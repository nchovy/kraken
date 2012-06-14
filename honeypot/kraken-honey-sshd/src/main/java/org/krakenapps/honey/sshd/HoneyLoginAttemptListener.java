package org.krakenapps.honey.sshd;

import java.net.InetSocketAddress;

public interface HoneyLoginAttemptListener {
	void onLoginAttempt(InetSocketAddress remote, String username, String password);
}
