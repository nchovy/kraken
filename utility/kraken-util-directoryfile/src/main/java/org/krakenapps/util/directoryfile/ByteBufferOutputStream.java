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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;

public class ByteBufferOutputStream extends OutputStream {

	private final MappedByteBuffer map;

	public ByteBufferOutputStream(MappedByteBuffer map) {
		this.map = map;
	}
	
	public MappedByteBuffer getMappedByteBuffer() {
		return map;
	}

	@Override
	public void write(int b) throws IOException {
		map.put((byte) b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		map.put(b);
	}

	@Override
	public void flush() throws IOException {
		map.force();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		map.put(b, off, len);
	}

	@Override
	public void close() throws IOException {
		map.force();
	}
	
	

}
