package org.krakenapps.honey.sshd;

import org.krakenapps.termio.TerminalSession;

public interface HoneySshSession extends TerminalSession {
	HoneySshService getHoneySshService();

	HoneyFileSystem getHoneyFileSystem();
}
