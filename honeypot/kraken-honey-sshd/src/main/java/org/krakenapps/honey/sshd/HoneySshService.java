package org.krakenapps.honey.sshd;

import java.io.File;
import java.io.IOException;

public interface HoneySshService {
	String getHostname();

	void setHostname(String hostname);

	File getRootPath();

	void setRootPath(File dir);

	void open() throws IOException;

	void close();
}
