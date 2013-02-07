/*
 * Copyright 2010 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.util.directoryfile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SplitDirectoryFileInputStream extends InputStream {
	private final long unitSize;
	private final File fileBase;

	private DirectoryFileArchive dfa;

	private InputStream stream;
	private int currentId;
	private int lastId;

	public SplitDirectoryFileInputStream(long unitSize, DirectoryFileArchive dfa, File file) throws IOException {
		this.unitSize = unitSize;
		this.fileBase = file;
		this.dfa = dfa;
		this.dfa.attach();
		init();
	}

	public SplitDirectoryFileInputStream(long unitSize, DirectoryFileArchive dfa, String name) throws IOException {
		this.unitSize = unitSize;
		this.fileBase = new File(name);
		this.dfa = dfa;
		this.dfa.attach();
		init();
	}

	protected InputStream getInputStream(File file) throws IOException {
		return dfa.getInputStreamAbsolutePath(getCurrentFile(fileBase, currentId).getAbsolutePath());
	}

	public DirectoryFileArchive getDirectoryFileArchive() {
		return this.dfa;
	}

	private void init() throws IOException {
		currentId = 0;
		lastId = SplitDirectoryFileOutputStream.getLastId(dfa, fileBase);
		if (lastId == -1)
			throw new IllegalStateException("getLastId should not return -1.");
		stream = getInputStream(getCurrentFile(fileBase, currentId));
	}

	private static File getCurrentFile(File base, int id) {
		if (id > 0)
			return new File(base.getPath() + "." + id);
		else
			return base;
	}

	@Override
	public int read() throws IOException {
		int ret = stream.read();
		if (ret == -1) {
			if (initNextStream())
				return stream.read();
			else
				return -1;
		}
		return ret;
	}

	private boolean initNextStream() throws IOException {
		currentId++;
		if (currentId > lastId)
			return false;
		stream.close();
		stream = getInputStream(getCurrentFile(fileBase, currentId));
		return true;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (len > unitSize) {
			return bulkRead(b, off, len);
		}

		int read = stream.read(b, off, len);
		if (read == -1) {
			if (initNextStream())
				return stream.read(b, off, len);
			else
				return -1;
		}

		if (read == len) {
			return read;
		} else {
			int remain = len - read;
			if (initNextStream()) {
				int anotherRead = stream.read(b, off + read, remain);
				if (anotherRead == -1)
					return read;
				return read + anotherRead;
			} else {
				return read;
			}
		}
	}

	private int bulkRead(byte[] b, int off, int len) throws IOException {
		// len > unitSize
		int currOff = off;
		int totalRead = 0;
		while (len > 0) {
			int read = stream.read(b, currOff, (int) unitSize);
			if (read == -1) {
				if (initNextStream()) {
					continue;
				} else {
					return totalRead;
				}
			} else {
				len -= read;
				totalRead += read;
			}
		}

		return totalRead;
	}

	@Override
	public long skip(long n) throws IOException {
		int available = stream.available();
		int skip = 0;
		if (n > available && initNextStream()) {
			n -= available;
			skip += available;
			while (n > unitSize) {
				initNextStream();
				n -= unitSize;
				skip += unitSize;
			}
			return stream.skip(n) + skip;
		} else {
			return stream.skip(n);
		}
	}

	@Override
	public int available() throws IOException {
		int remainingFiles = lastId - currentId;
		// @formatter:off
		if (remainingFiles <= 0)
			return stream.available();
		if (remainingFiles == 1) {
			return (int) (stream.available() 
					+ dfa.getActualSize(dfa.getSubPath(SplitDirectoryFileOutputStream.getLastFile(fileBase, lastId))));			
		} else {
			return (int) (stream.available() 
					+ unitSize * (remainingFiles - 2) 
					+ dfa.getActualSize(dfa.getSubPath(SplitDirectoryFileOutputStream.getLastFile(fileBase, lastId))));
		}
		// @formatter:on
	}

	@Override
	public void close() throws IOException {
		stream.close();
		dfa.close();
	}

	@Override
	public boolean markSupported() {
		return false;
	}
}
