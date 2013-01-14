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
import java.io.IOException;
import java.util.NoSuchElementException;

import org.krakenapps.logstorage.file.BufferedRandomAccessFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvertedIndexReader {
	private final Logger logger = LoggerFactory.getLogger(InvertedIndexReader.class.getName());

	private boolean closed;
	private File indexFile;
	private File dataFile;

	private BufferedRandomAccessFileReader indexReader;
	private BufferedRandomAccessFileReader dataReader;

	public static void main(String[] args) throws IOException {
		File indexFile = new File("D:/8bug/data/kraken-logstorage/index/10/2012-12-05.pos");
		File dataFile = new File("D:/8bug/data/kraken-logstorage/index/10/2012-12-05.seg");

		InvertedIndexReader reader = new InvertedIndexReader(indexFile, dataFile);
		InvertedIndexCursor cursor = reader.openCursor("98.84.233.191");
		while (cursor.hasNext())
			System.out.println(cursor.next());
	}

	public InvertedIndexReader(File indexFile, File dataFile) throws IOException {
		this.indexFile = indexFile;
		this.dataFile = dataFile;

		indexReader = new BufferedRandomAccessFileReader(indexFile);
		dataReader = new BufferedRandomAccessFileReader(dataFile);
	}

	public File getIndexFile() {
		return indexFile;
	}

	public File getDataFile() {
		return dataFile;
	}

	public InvertedIndexCursor openCursor(String term) throws IOException {
		return new Cursor(term);
	}

	public class Cursor implements InvertedIndexCursor {
		// end position of segment, also recorded in index file
		private long dataEndOffset;

		// current data read position
		private long dataPos;

		// current loaded segment index (numbering from 0)
		private long currentSegmentIndex;

		// total posting length of current loaded segment
		private long currentPostingCount;

		// remaining posting count of current loaded segment (descreasing)
		private long remaining;

		// next will return this item
		private Long prefetch = null;

		// last item for delta decoding
		private long last;

		// search target term
		private String term;

		public Cursor(String term) throws IOException {
			this.term = term;

			// align
			currentSegmentIndex = (indexFile.length() >> 3) - 1;
			// backward segment traverse until term matches
			Long postingCount = null;
			while (currentSegmentIndex >= 0) {
				postingCount = loadSegment(currentSegmentIndex);
				if (postingCount != null) {
					currentPostingCount = postingCount;
					remaining = postingCount;
					break;
				}

				currentSegmentIndex--;
			}
		}

		private boolean loadNextSegment() throws IOException {
			System.out.println("load next seg: " + currentSegmentIndex);
			Long postingCount = null;
			while (currentSegmentIndex > 0) {
				postingCount = loadSegment(--currentSegmentIndex);
				if (postingCount != null) {
					currentPostingCount = postingCount;
					remaining = postingCount;
					return true;
				}
			}

			return false;
		}

		@Override
		public boolean hasNext() {
			if (closed) {
				return false;
			}

			try {
				if (prefetch != null)
					return true;

				if (remaining <= 0) {
					if (!loadNextSegment())
						return false;
				}

				if (remaining == currentPostingCount) {
					prefetch = nextId();
				} else {
					prefetch = last - nextId();
				}

				last = prefetch;
				remaining--;

				return prefetch != null;
			} catch (IOException e) {
				return false;
			}
		}

		private Long loadSegment(long segmentIndex) throws IOException {
			indexReader.seek(segmentIndex << 3);
			dataEndOffset = indexReader.readLong();
			dataPos = dataEndOffset;
			logger.debug("kraken logstorage: index data end offset [{}]", dataEndOffset);
			dataReader.seek(dataEndOffset);

			long version = nextLong();
			if (version != 1)
				throw new IllegalStateException("block version is not supported " + version);

			long termBlockLength = nextLong();
			logger.debug("kraken logstorage: index term block length [{}]", termBlockLength);

			long postingBlockLength = nextLong();
			logger.debug("kraken logstorage: posting block length [{}]", postingBlockLength);

			// find target term while iterate
			long beginOfTermBlock = dataPos - termBlockLength;
			logger.debug("kraken logstorage: begin [{}], data pos [{}]", beginOfTermBlock, dataPos);

			Long postingOffset = null;
			Long postingCount = null;
			while (dataPos > beginOfTermBlock) {
				long termLen = nextLong();
				String t = nextString(termLen);
				long termCount = nextLong();
				long offset = nextLong();

				if (t.equalsIgnoreCase(term)) {
					postingOffset = offset;
					postingCount = termCount;
					break;
				}

				if (logger.isDebugEnabled())
					logger.debug("kraken logstorage: term {}, count {}, offset {}", new Object[] { t, termCount, offset });
			}

			if (postingOffset == null)
				return null;

			// relative to absolute position
			long size = 1 + InvertedIndexWriter.lengthOfRawNumber(long.class, termBlockLength)
					+ InvertedIndexWriter.lengthOfRawNumber(long.class, postingBlockLength) + termBlockLength
					+ postingBlockLength;

			dataPos = dataEndOffset - size + 1 + postingOffset;
			logger.debug("kraken logstorage: data [{}~{}] term [{}] posting block length [{}] posting offset [{}]", new Object[] {
					dataPos, dataEndOffset, termBlockLength, postingBlockLength, postingOffset });

			dataReader.seek(dataPos);
			return postingCount;
		}

		@Override
		public long next() throws IOException {
			if (closed) {
				String msg = "index reader is already closed, index=" + indexFile.getAbsolutePath() + ", data="
						+ dataFile.getAbsolutePath();
				throw new IOException(msg);
			}

			if (prefetch == null)
				throw new NoSuchElementException();

			long n = prefetch;
			prefetch = null;
			return n;
		}

		private long nextId() throws IOException {
			long value = 0L;

			byte b;
			do {
				value = value << 7;
				b = dataReader.readByte();
				value |= b & 0x7F;
			} while ((b & 0x80) == 0x80);
			return value;
		}

		private long nextLong() throws IOException {
			long l = 0;
			byte b;
			do {
				dataReader.seek(dataPos);
				l <<= 7;
				b = dataReader.readByte();
				dataPos--;
				l |= b & 0x7f;
			} while ((b & 0x80) != 0);
			return l;
		}

		private String nextString(long len) throws IOException {
			dataPos -= len - 1;
			dataReader.seek(dataPos);
			byte[] b = new byte[(int) len];
			dataReader.readFully(b);
			dataReader.seek(dataPos);
			dataPos--;
			return new String(b, "utf-8");
		}
	}

	public void close() {
		if (closed)
			return;

		closed = true;

		try {
			indexReader.close();
		} catch (IOException e) {
		}

		try {
			dataReader.close();
		} catch (IOException e) {
		}
	}
}
