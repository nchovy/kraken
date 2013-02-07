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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class SplitDirectoryFileOutputStream extends OutputStream {
	protected final long unitSize;
	protected final File fileBase;
	protected final boolean append;
	protected final DirectoryFileArchive dfa;
	
	private OutputStream stream = null;
	private long currentStreamLength;
	private int lastId;
	
	public SplitDirectoryFileOutputStream(long unitSize, DirectoryFileArchive dfa, File file, boolean append) throws IOException {
		this.unitSize = unitSize;
		this.fileBase = file;
		this.append = append;
		this.dfa = dfa;
		this.dfa.attach();
		init();
	}

	public SplitDirectoryFileOutputStream(long unitSize, DirectoryFileArchive dfa, File file) throws IOException {
		this.unitSize = unitSize;
		this.fileBase = file;
		this.append = false;
		this.dfa = dfa;
		this.dfa.attach();
		init();
	}

	public SplitDirectoryFileOutputStream(long unitSize, DirectoryFileArchive dfa, String name, boolean append) throws IOException {
		this.unitSize = unitSize;
		this.fileBase = new File(name);
		this.append = append;
		this.dfa = dfa;
		this.dfa.attach();
		init();
	}

	public SplitDirectoryFileOutputStream(long unitSize, DirectoryFileArchive dfa, String name) throws IOException {
		this.unitSize = unitSize;
		this.fileBase = new File(name);
		this.append = false;
		this.dfa = dfa;
		this.dfa.attach();
		init();
	}

	protected OutputStream getOutputStream(File lastFile, boolean append) throws IOException {
		return dfa.getOutputStreamAbsolutePath(lastFile.getAbsolutePath(), (int) unitSize);
	}
	
	public DirectoryFileArchive getDirectoryFileArchive() {
		return this.dfa;
	}
	
	private void init() throws IOException {
		if (append) {
			// find last file.
			lastId = getLastId(dfa, this.fileBase);
			File lastFile = getLastFile(this.fileBase, lastId);
			stream = getOutputStream(lastFile, append);
			currentStreamLength = lastFile.length();
		} else {
			// remove existing files.
			int l = getLastId(dfa, this.fileBase);
			for (int i = 1; i <= l; ++i) {
				File c = new File(fileBase.getPath() + "." + i);
				c.delete();
			}
			new File(fileBase.getPath() + ".lastId").delete();
			lastId = 0;
			stream = getOutputStream(fileBase, false);
			currentStreamLength = 0;
		}

		if (currentStreamLength >= unitSize) {
			initNextStream();
		}
	}

	private void initNextStream() throws IOException {
		lastId++;
		dfa.setReservedAbsolutePath(fileBase.getAbsolutePath(), lastId);
		stream.close();
		stream = getOutputStream(getLastFile(fileBase, lastId), false);
		currentStreamLength = 0;
	}

	public static int getLastId(DirectoryFileArchive dfa, File base) {
		if (dfa.exists(base)) {
			try {
				long lastId = dfa.getReservedAbsolutePath(base.getAbsolutePath());
				return (int) lastId;
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
		
		return 0;
	}

	public static File getLastFile(File base, int lastId) throws FileNotFoundException {
		if (lastId > 0) {
			return new File(base.getPath() + "." + lastId);
		} else {
			return base;
		}
	}

	public void close() throws IOException {
		if (stream != null)
			stream.close();
		if (dfa != null)
			dfa.close();
	}

	@Override
	public void write(int b) throws IOException {
		if (currentStreamLength < unitSize)
			stream.write(b);
		else {
			initNextStream();
			stream.write(b);
		}
		currentStreamLength++;
	}

	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		long expected = unitSize - (currentStreamLength + len);
		if (expected > 0) {
			stream.write(b, off, len);
			currentStreamLength += len;
		} else if (expected < 0) {
			int middle = (int) (len + expected);
			stream.write(b, off, middle);
			initNextStream();
			stream.write(b, off + middle, (int) -expected);
			currentStreamLength += -expected;
		} else {
			stream.write(b, off, len);
			initNextStream();
		}
	}

	@Override
	public void flush() throws IOException {
		if (stream != null)
			stream.flush();
	}

}
