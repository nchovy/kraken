package org.krakenapps.termio;

public interface TerminalEventListener {
	void onConnected(TerminalSession session);

	void onCommand(TerminalSession session, int command, byte[] option);

	void onData(TerminalSession session, int b);
}
