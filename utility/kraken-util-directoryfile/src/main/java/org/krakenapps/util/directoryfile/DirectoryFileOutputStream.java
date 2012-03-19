package org.krakenapps.util.directoryfile;

import java.io.IOException;
import java.nio.MappedByteBuffer;

public class DirectoryFileOutputStream extends ByteBufferOutputStream {
	private final String subPath;
	private final DirectoryFileArchive dfa;

	public DirectoryFileOutputStream(DirectoryFileArchive dfa, MappedByteBuffer map, String subPath) {
		super(map);
		this.dfa = dfa;
		dfa.attach();
		this.subPath = subPath;
	}

	public String getSubPath() {
		return subPath;
	}

	public void close() throws IOException {
		try {
			super.close();
			dfa.setActualSize(subPath, this.getMappedByteBuffer().position());
		} finally {
			dfa.close();
		}
	}
}
