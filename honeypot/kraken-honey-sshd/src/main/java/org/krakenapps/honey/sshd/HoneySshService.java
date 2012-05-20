package org.krakenapps.honey.sshd;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface HoneySshService {
	String getHostname();

	void setHostname(String hostname);

	File getRootPath();

	void setRootPath(File dir);

	void open() throws IOException;

	void close();

	Collection<HoneyLoginAttemptListener> getLoginAttemptListeners();

	void addLoginAttemptListener(HoneyLoginAttemptListener listener);

	void removeLoginAttemptListener(HoneyLoginAttemptListener listener);
}
