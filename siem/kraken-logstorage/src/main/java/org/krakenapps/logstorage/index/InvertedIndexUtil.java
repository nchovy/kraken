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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Map;

import org.krakenapps.codec.EncodingRule;
import org.krakenapps.codec.FastEncodingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 0.9
 * @author xeraph
 * 
 */
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

	public static void merge(InvertedIndexFileSet older, InvertedIndexFileSet newer, InvertedIndexFileSet merged)
			throws IOException {
		if (merged.getIndexFile().exists() && merged.getIndexFile().length() > 0)
			throw new IllegalStateException("merged index file should be empty: " + merged.getIndexFile().getAbsolutePath());

		if (merged.getDataFile().exists() && merged.getDataFile().length() > 0)
			throw new IllegalStateException("merged index file should be empty: " + merged.getDataFile().getAbsolutePath());

		// validate file headers
		readHeader(older.getIndexFile());
		readHeader(older.getDataFile());
		InvertedIndexHeader newerIndexHeader = readHeader(newer.getIndexFile());
		InvertedIndexHeader newerDataHeader = readHeader(newer.getDataFile());

		// copy old index and data file
		BufferedInputStream olderPosStream = null;
		BufferedInputStream olderSegStream = null;

		BufferedInputStream newerPosStream = null;
		BufferedInputStream newerSegStream = null;

		BufferedOutputStream mergedPosStream = null;
		BufferedOutputStream mergedSegStream = null;

		try {
			olderPosStream = new BufferedInputStream(new FileInputStream(older.getIndexFile()));
			olderSegStream = new BufferedInputStream(new FileInputStream(older.getDataFile()));
			newerPosStream = new BufferedInputStream(new FileInputStream(newer.getIndexFile()));
			newerSegStream = new BufferedInputStream(new FileInputStream(newer.getDataFile()));
			mergedPosStream = new BufferedOutputStream(new FileOutputStream(merged.getIndexFile()));
			mergedSegStream = new BufferedOutputStream(new FileOutputStream(merged.getDataFile()));

			byte[] b = new byte[8096];

			// copy old files first
			long dataLen = 0;
			while (true) {
				int len = olderSegStream.read(b);
				if (len <= 0)
					break;
				mergedSegStream.write(b, 0, len);
				dataLen += len;
			}

			while (true) {
				int len = olderPosStream.read(b);
				if (len <= 0)
					break;
				mergedPosStream.write(b, 0, len);
			}

			// append newer segment body part
			newerSegStream.skip(newerDataHeader.getBodyOffset());
			while (true) {
				int len = newerSegStream.read(b);
				if (len <= 0)
					break;
				mergedSegStream.write(b, 0, len);
			}

			// append newer position body part, need to adjust position
			long adjust = dataLen - newerDataHeader.getBodyOffset();

			newerPosStream.skip(newerIndexHeader.getBodyOffset());
			byte[] posbuf = new byte[8];

			while (true) {
				int len = newerPosStream.read(posbuf);
				if (len <= 0)
					break;

				long pos = ByteBuffer.wrap(posbuf).getLong();
				pos += adjust;
				prepareLong(pos, posbuf);
				mergedPosStream.write(posbuf);
			}
		} finally {
			ensureClose(olderPosStream);
			ensureClose(olderSegStream);
			ensureClose(newerPosStream);
			ensureClose(newerSegStream);
			ensureClose(mergedPosStream);
			ensureClose(mergedSegStream);
		}
	}

	public static void prepareLong(long l, byte[] b) {
		for (int i = 0; i < 8; i++)
			b[i] = (byte) ((l >> ((7 - i) * 8)) & 0xff);
	}

	private static void ensureClose(InputStream is) {
		Logger logger = LoggerFactory.getLogger(InvertedIndexUtil.class);
		try {
			if (is != null)
				is.close();
		} catch (Throwable t) {
			logger.error("kraken logstorage: cannot close file while index merging", is);
		}
	}

	private static void ensureClose(OutputStream os) {
		Logger logger = LoggerFactory.getLogger(InvertedIndexUtil.class);
		try {
			if (os != null)
				os.close();
		} catch (Throwable t) {
			logger.error("kraken logstorage: cannot close file while index merging", os);
		}
	}

}
