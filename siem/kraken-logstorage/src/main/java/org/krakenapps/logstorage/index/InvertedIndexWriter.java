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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 0.9
 * @author xeraph
 */
public class InvertedIndexWriter {
	private final Logger logger = LoggerFactory.getLogger(InvertedIndexWriter.class.getName());
	private final int FLUSH_THRESHOLD = 10000;
	private final Charset utf8 = Charset.forName("utf-8");
	private int queueCount;

	// term -> log id postings (no key)
	private Map<String, List<InvertedIndexItem>> postings;

	private boolean closed;

	private InvertedIndexFileSet files;
	private OutputStream indexStream;
	private OutputStream dataStream;

	// total index data length (will be used for index position marking)
	private long dataLength;

	private byte[] longbuf = new byte[8];

	private Date lastFlush = new Date();

	public InvertedIndexWriter(File indexFile, File dataFile) throws IOException {
		this(new InvertedIndexFileSet(indexFile, dataFile));
	}

	public InvertedIndexWriter(InvertedIndexFileSet files) throws IOException {
		this.postings = new HashMap<String, List<InvertedIndexItem>>();

		if (isEmptyFile(files.getIndexFile()) && isEmptyFile(files.getDataFile())) {
			// write file header if empty
			Map<String, Object> indexHeaders = new HashMap<String, Object>();
			indexHeaders.put("version", 1);
			indexHeaders.put("type", "pos");
			indexHeaders.put("created", new Date());

			Map<String, Object> dataHeaders = new HashMap<String, Object>();
			dataHeaders.put("version", 1);
			dataHeaders.put("type", "seg");
			dataHeaders.put("created", new Date());

			InvertedIndexHeader indexHeader = new InvertedIndexHeader(indexHeaders);
			InvertedIndexHeader dataHeader = new InvertedIndexHeader(dataHeaders);

			InvertedIndexUtil.writeHeader(indexHeader, files.getIndexFile());
			InvertedIndexUtil.writeHeader(dataHeader, files.getDataFile());
		} else {
			// check file header
			InvertedIndexUtil.readHeader(files.getIndexFile());
			InvertedIndexUtil.readHeader(files.getDataFile());
		}

		// open file stream
		this.indexStream = new FileOutputStream(files.getIndexFile(), true);
		this.dataStream = new BufferedOutputStream(new FileOutputStream(files.getDataFile(), true));
		this.dataLength = files.getDataFile().length();
	}

	private boolean isEmptyFile(File f) {
		if (!f.exists())
			return true;

		if (f.isFile() && f.length() == 0)
			return true;

		return false;
	}

	public void write(InvertedIndexItem item) throws IOException {
		if (closed) {
			String msg = "inverted index writer is closed: index=" + files.getIndexFile().getAbsolutePath() + ", data="
					+ files.getDataFile().getAbsolutePath();
			throw new IllegalStateException(msg);
		}

		if (item.tokens != null) {
			for (String t : item.tokens) {
				if (t == null)
					continue;

				List<InvertedIndexItem> items = postings.get(t);
				if (items == null) {
					items = new ArrayList<InvertedIndexItem>();
					postings.put(t, items);
				}

				items.add(item);
			}
		}

		if (queueCount >= FLUSH_THRESHOLD)
			flush();

		queueCount++;
	}

	public void flush() throws IOException {
		if (postings.isEmpty())
			return;

		// mark last flush time
		lastFlush = new Date();

		Map<String, Term> terms = new TreeMap<String, Term>();

		// posting block length
		long pblen = 0;

		// postings block for specific term
		for (Entry<String, List<InvertedIndexItem>> e : postings.entrySet()) {
			long plen = 0;
			List<InvertedIndexItem> v = e.getValue();

			Collections.reverse(v);
			Collections.sort(v);

			// mark postings block begin position
			terms.put(e.getKey(), new Term(v.size(), pblen));

			// write postings
			Long last = null;
			for (InvertedIndexItem item : v) {
				if (last == null) {
					plen += writeBeNumber(long.class, item.id);
				} else {
					plen += writeBeNumber(long.class, last - item.id);
				}
				last = item.id;
			}

			pblen += plen;
		}

		// term block length
		long tblen = 0;

		// write term block
		for (Entry<String, Term> e : terms.entrySet()) {
			tblen += writeLeNumber(long.class, e.getValue().offset);
			tblen += writeLeNumber(long.class, e.getValue().count);
			byte[] token = e.getKey().getBytes(utf8);
			dataStream.write(token);
			tblen += token.length;
			tblen += writeLeNumber(int.class, token.length);
		}

		// write posting block length
		int plen = writeLeNumber(long.class, pblen);

		// write term block length
		logger.debug("kraken logstorage: writing term block length {}", tblen);
		int tlen = writeLeNumber(long.class, tblen);

		// last version mark
		dataStream.write(1);
		dataStream.flush();

		dataLength += plen + tlen + tblen + pblen + 1;

		// write end offset of block to index
		InvertedIndexUtil.prepareLong(dataLength - 1, longbuf);
		logger.debug("kraken logstorage: writing index data offset [{}]", (dataLength - 1));
		indexStream.write(longbuf);

		queueCount = 0;
		postings.clear();
	}

	private int writeBeNumber(Class<?> clazz, long value) throws IOException {
		int len = lengthOfRawNumber(clazz, value);
		for (int i = 0; i < len; i++) {
			byte signalBit = (byte) (i != len - 1 ? 0x80 : 0);
			byte data = (byte) (signalBit | (byte) (value >> (7 * (len - i - 1)) & 0x7F));
			dataStream.write(data);
		}
		return len;
	}

	// little endian writing
	private int writeLeNumber(Class<?> clazz, long value) throws IOException {
		int len = lengthOfRawNumber(clazz, value);
		for (int i = 0; i < len; i++) {
			byte signalBit = (byte) (i == 0 ? 0 : 0x80);
			byte data = (byte) (signalBit | (byte) (value & 0x7F));
			value >>= 7;
			dataStream.write(data);
		}
		return len;
	}

	public static <T> int lengthOfRawNumber(Class<T> clazz, long value) {
		if (value < 0) {
			if (long.class == clazz)
				return 10; // max length for long
			else if (int.class == clazz)
				return 5; // max length for int
			else
				return 3; // max length for short
		} else {
			if (value <= 127)
				return 1;
			if (value <= 16383)
				return 2;
		}

		return (63 - Long.numberOfLeadingZeros(value)) / 7 + 1;
	}

	public Date getLastFlush() {
		return lastFlush;
	}

	public void close() {
		if (closed)
			return;

		closed = true;

		try {
			flush();
		} catch (IOException e) {
		}

		try {
			indexStream.close();
			indexStream = null;
		} catch (IOException e) {
		}

		try {
			dataStream.close();
			dataStream = null;
		} catch (IOException e) {
		}
	}

	private static class Term {
		// term count
		public long count;

		// posting offset
		public long offset;

		public Term(long count, long offset) {
			this.count = count;
			this.offset = offset;
		}
	}
}
