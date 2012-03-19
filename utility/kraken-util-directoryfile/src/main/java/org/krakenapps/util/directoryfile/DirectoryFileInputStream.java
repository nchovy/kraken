package org.krakenapps.util.directoryfile;

import java.io.IOException;
import java.nio.MappedByteBuffer;

public class DirectoryFileInputStream extends ByteBufferInputStream {
	private final String subPath;
	private final DirectoryFileArchive dfa;

	public DirectoryFileInputStream(DirectoryFileArchive dfa, MappedByteBuffer map, String subPath) {
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
		} finally {
			dfa.close();
		}
	}
}
