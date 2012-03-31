package org.krakenapps.honey.sshd;

import java.io.IOException;

public interface HoneySshService {
	String getHostname();

	void setHostname(String hostname);

	void open() throws IOException;

	void close();
}
