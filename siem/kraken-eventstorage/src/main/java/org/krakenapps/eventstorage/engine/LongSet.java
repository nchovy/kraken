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
package org.krakenapps.eventstorage.engine;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LongSet extends AbstractSet<Long> {
	private static final int DEFAULT_CACHE_SIZE = 50000;

	private File file;
	private RandomAccessFile raf;

	private Set<Long> cache;
	private int cacheSize;
	private Object cacheLock = new Object();

	private Map<Short, Long> fps = new HashMap<Short, Long>();
	private BufferedRandomAccessFile block1;
	private Object block1lock = new Object();
	private long nextFp1;
	private BufferedRandomAccessFile block2;
	private Object block2lock = new Object();
	private long nextFp2;

	private int size;

	public LongSet() {
		this(DEFAULT_CACHE_SIZE);
	}

	public LongSet(int cacheSize) {
		this.cache = new HashSet<Long>(cacheSize);
		this.cacheSize = cacheSize;
	}

	@Override
	public boolean add(Long e) {
		if (contains(e))
			return false;

		synchronized (cacheLock) {
			if (!cache.add(e))
				return false;
		}

		if (cache.size() >= cacheSize) {
			try {
				flush();
			} catch (IOException e1) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean contains(Object o) {
		try {
			if (!(o instanceof Long))
				return false;

			if (cache.contains(o))
				return true;

			Long e = (Long) o;
			short key1 = (short) ((e >> 32) & 0xFFFF);
			int key2 = (int) ((e >> 16) & 0xFFFF);
			int key3 = (int) (e & 0xFFFF);

			Long fp1 = fps.get(key1);
			if (fp1 == null || raf == null)
				return false;

			long fp2 = 0L;
			synchronized (block1lock) {
				block1.seek(fp1 + key2 * 8);
				fp2 = block1.readLong();
			}
			if (fp2 == 0L)
				return false;

			synchronized (block2lock) {
				block2.seek(fp2 + key3 / 8);
				return (block2.readByte() & (1 << (key3 % 8))) != 0;
			}
		} catch (IOException e) {
			return false;
		}
	}

	private synchronized void flush() throws IOException {
		if (raf == null) {
			File dir = new File(System.getProperty("kraken.data.dir"), "kraken-eventstorage/data/");
			if (!dir.exists())
				dir.mkdirs();
			file = new File(dir, "longset" + System.currentTimeMillis() + ".tmp");
			raf = new RandomAccessFile(file, "rw");
			raf.setLength(0);
			block1 = new BufferedRandomAccessFile(raf, 512 * 1024);
			nextFp1 = 0L;
			block2 = new BufferedRandomAccessFile(raf, 8 * 1024);
			nextFp2 = 524288L;
		}

		List<Long> sorted = null;
		synchronized (cacheLock) {
			sorted = new ArrayList<Long>(cache);
			cache.clear();
		}
		Collections.sort(sorted);

		for (Long e : sorted) {
			short key1 = (short) ((e >>> 32) & 0xFFFF);
			int key2 = (int) (((e >>> 16) & 0xFFFF) << 3);
			int key3 = (int) (e & 0xFFFF);

			Long fp1 = fps.get(key1);
			if (fp1 == null) {
				fps.put(key1, nextFp1);
				fp1 = nextFp1;
				nextFp1 += 524288L;
				if (nextFp1 <= nextFp2)
					nextFp1 = (nextFp2 / 524288L + 1L) * 524288L;
			}
			fp1 += key2;

			long fp2 = 0L;
			synchronized (block1lock) {
				block1.seek(fp1);
				fp2 = block1.readLong();
				if (fp2 == 0) {
					fp2 = nextFp2;
					block1.seek(fp1);
					block1.writeLong(fp2);
					nextFp2 += 8192L;
					if (nextFp2 % 524288 == 0 && nextFp2 <= nextFp1)
						nextFp2 = nextFp1 + 524288L;
				}
			}
			fp2 += key3 / 8;

			synchronized (block2lock) {
				block2.seek(fp2);
				byte b = (byte) ((block2.readByte() & 0xFF) | (1 << (key3 % 8)));
				block2.seek(fp2);
				block2.write(b);
			}
		}
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Long> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		return size;
	}

	public void close() {
		if (raf != null) {
			try {
				raf.close();
			} catch (IOException e) {
			}
			file.delete();
		}
	}
}
