package org.krakenapps.honey.sshd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface HoneyFileSystem {
	List<HoneyPath> listFiles(HoneyPath path) throws IOException;

	void mkdirs(HoneyPath path) throws IOException;

	OutputStream create(HoneyPath path) throws IOException;

	InputStream open(HoneyPath path) throws IOException;

	boolean rename(HoneyPath src, HoneyPath dst) throws IOException;

	void delete(HoneyPath path) throws IOException;

}
