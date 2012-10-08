/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.logdb.sort;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import org.krakenapps.codec.EncodingRule;

class FileRunIterator implements CloseableIterator {
	private static final int READ_BUFFER_SIZE = 1024 * 128;
	private InputStream bis;
	private Object next;
	private byte[] buf = new byte[640 * 1024];
	private byte[] intbuf = new byte[4];
	private long totalRead;

	public FileRunIterator(File f) throws IOException {
		bis = new BufferedInputStream(new FileInputStream(f), READ_BUFFER_SIZE);
	}

	@Override
	public boolean hasNext() {
		if (next == null) {
			try {
				int readBytes = IoHelper.ensureRead(bis, intbuf, 4);
				totalRead += readBytes;

				if (readBytes == 4) {
					int len = IoHelper.decodeInt(intbuf);
					readBytes = IoHelper.ensureRead(bis, buf, len);
					totalRead += readBytes;
					if (readBytes == len)
						next = EncodingRule.decode(ByteBuffer.wrap(buf, 0, len));
				}
			} catch (IOException e) {
				try {
					close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		return next != null;
	}

	@Override
	public Object next() {
		if (!hasNext())
			throw new NoSuchElementException();

		Object ret = next;
		next = null;
		return ret;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
		bis.close();
	}
}
