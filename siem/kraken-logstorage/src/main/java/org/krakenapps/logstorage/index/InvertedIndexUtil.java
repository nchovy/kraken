/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logstorage.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Map;

import org.krakenapps.codec.EncodingRule;
import org.krakenapps.codec.FastEncodingRule;

public class InvertedIndexUtil {
	private static final String MAGIC_STRING = "KRAKEN_INVERTED_INDEX";
	private static final int MAGIC_LEN = MAGIC_STRING.getBytes().length;
	private static final int HEADER_VERSION_BYTES = 1;
	private static final int HEADER_LEN_BYTES = 2;
	private static final int HEADER_PREFIX_LEN = MAGIC_LEN + HEADER_VERSION_BYTES + HEADER_LEN_BYTES;

	private InvertedIndexUtil() {
	}

	public static void writeHeader(InvertedIndexHeader header, File file) throws IOException {
		if (file == null)
			throw new IllegalArgumentException("file should be not null");

		if (file.exists() && file.length() != 0)
			throw new IOException("index file is not empty, " + file.getAbsolutePath());

		FastEncodingRule enc = new FastEncodingRule();

		int ver = header.getVersion();
		Map<String, Object> headers = header.getHeaders();
		headers.remove("version");
		ByteBuffer bb = enc.encode(headers);

		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "rw");
			raf.write(MAGIC_STRING.getBytes());
			raf.write(ver);
			raf.writeShort(bb.remaining());
			raf.write(bb.array());
		} finally {
			if (raf != null)
				raf.close();
		}
	}

	public static InvertedIndexHeader readHeader(File file) throws IOException {

		if (file == null)
			throw new IllegalArgumentException("file should be not null");

		if (!file.exists())
			throw new FileNotFoundException(file.getAbsolutePath());

		if (!file.canRead())
			throw new IOException("check permission of file " + file.getAbsolutePath());

		// magic and header length bytes is essential
		long fileLen = file.length();
		if (fileLen < HEADER_PREFIX_LEN)
			throw new IOException("invalid inverted index file, short file length=" + fileLen + ", file="
					+ file.getAbsolutePath());

		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "r");
			byte[] b = new byte[MAGIC_LEN];
			raf.read(b);

			if (!MAGIC_STRING.equals(new String(b)))
				throw new IOException("invalid index file, magic string does not match, file=" + file.getAbsolutePath());

			int version = raf.read();
			if (version != 1)
				throw new IOException("invalid index file, version " + version + " is not supported, file="
						+ file.getAbsolutePath());

			int headerLength = raf.readShort() & 0xffff;
			if (fileLen < HEADER_PREFIX_LEN + headerLength)
				throw new IOException("invalid index file, broken header, file=" + file.getAbsolutePath());

			// TODO: read fully
			byte[] headerBlob = new byte[headerLength];
			raf.read(headerBlob);

			Map<String, Object> headers = EncodingRule.decodeMap(ByteBuffer.wrap(headerBlob));
			headers.put("version", version);

			InvertedIndexHeader h = new InvertedIndexHeader(headers);
			h.setBodyOffset(HEADER_PREFIX_LEN + headerLength);
			return h;
		} finally {
			if (raf != null)
				raf.close();
		}
	}

//	public static void merge(InvertedIndexFileSet older, InvertedIndexFileSet newer) {
//	}

}
