package org.krakenapps.honey.sshd;

public interface HoneyCommandHandler {

	int main(String[] args);

	HoneySshSession getSession();

	void setSession(HoneySshSession session);
}
