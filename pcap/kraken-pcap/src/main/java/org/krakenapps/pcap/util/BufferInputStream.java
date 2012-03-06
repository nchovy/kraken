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
package org.krakenapps.pcap.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;

/**
 * BufferInputStream translates calls to InputStream into calls to the Buffer.
 * 
 * @author mindori
 */
public class BufferInputStream extends InputStream {
	private Buffer buffer;

	public BufferInputStream(Buffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public int read() throws IOException {
		try {
			return (int) (buffer.get() & 0xff);
		} catch (BufferUnderflowException e) {
		}
		return -1;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return super.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return super.read(b, off, len);
	}

	@Override
	public int available() throws IOException {
		return super.available();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public void close() throws IOException {
		super.close();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public synchronized void mark(int readlimit) {
		throw new UnsupportedOperationException();
	}

	public void mark() {
		buffer.mark();
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public synchronized void reset() throws IOException {
		buffer.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return super.skip(n);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
