package org.krakenapps.termio;

public interface TerminalEventListener {
	void onConnected(TerminalSession session);

	void onDisconnected(TerminalSession session);

	void onCommand(TerminalSession session, int command, byte[] option);

	void onData(TerminalSession session, byte b);
}
