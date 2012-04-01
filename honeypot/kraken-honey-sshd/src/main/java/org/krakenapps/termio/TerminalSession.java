package org.krakenapps.termio;

import java.net.InetSocketAddress;
import java.util.Set;

public interface TerminalSession {
	String getUsername();

	InetSocketAddress getRemoteAddress();

	String getEnvironmentVariable(String key);

	void setEnvironmentVariable(String key, String value);

	TerminalInputStream getInputStream();

	void setInputStream(TerminalInputStream in);

	TerminalOutputStream getOutputStream();

	void setOutputStream(TerminalOutputStream out);

	void close();

	Set<TerminalEventListener> getListeners();

	void addListener(TerminalEventListener listener);

	void removeListener(TerminalEventListener listener);
}
