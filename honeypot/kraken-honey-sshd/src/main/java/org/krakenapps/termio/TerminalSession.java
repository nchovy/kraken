package org.krakenapps.termio;

import java.io.IOException;
import java.util.Set;

public interface TerminalSession {
	String getUsername();

	String getEnvironmentVariable(String key);

	void setEnvironmentVariable(String key, String value);

	void write(int b) throws IOException;

	void write(byte[] b) throws IOException;

	void flush() throws IOException;

	Set<TerminalEventListener> getListeners();

	void addListener(TerminalEventListener listener);

	void removeListener(TerminalEventListener listener);
}
