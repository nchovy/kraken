/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.logdb.query;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.krakenapps.codec.CustomCodec;
import org.krakenapps.codec.EncodingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class FileBufferMap<K, V> implements Map<K, V> {
	private static File BASE_DIR = new File(System.getProperty("kraken.data.dir"), "kraken-logdb/query/");
	private static int BYTEBUFFER_CAPACITY = 655360; // 640KB
	private Logger logger = LoggerFactory.getLogger(FileBufferMap.class);
	private ByteBuffer bb;
	private CustomCodec cc;

	private File file;
	private RandomAccessFile raf;
	private long rafLength;
	private ConcurrentMap<K, V> cache = new ConcurrentHashMap<K, V>();
	private int cacheSize;
	private Map<K, Block> fp = new HashMap<K, Block>();
	private PriorityQueue<Block> freeBlock = new PriorityQueue<Block>(11, new BlockComparator()); // unused
	private V classinfo = null;

	public FileBufferMap() throws IOException {
		this(10000);
	}

	public FileBufferMap(int cacheSize) throws IOException {
		this(cacheSize, null);
	}

	public FileBufferMap(CustomCodec cc) throws IOException {
		this(10000, cc);
	}

	public FileBufferMap(int cacheSize, CustomCodec cc) throws IOException {
		if (!BASE_DIR.exists())
			BASE_DIR.mkdirs();
		this.bb = ByteBuffer.allocate(BYTEBUFFER_CAPACITY);
		this.file = File.createTempFile("fbm", ".buf", BASE_DIR);
		this.file.deleteOnExit();
		this.raf = new RandomAccessFile(file, "rw");
		this.cacheSize = cacheSize;
		this.cc = cc;
	}

	@Override
	public int size() {
		return fp.size();
	}

	@Override
	public boolean isEmpty() {
		return fp.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return fp.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		if (!cache.containsKey(key) && fp.containsKey(key)) {
			try {
				Block b = fp.get(key);
				raf.seek(b.fp);
				raf.read(bb.array(), 0, b.size);
				Object obj = EncodingRule.decode(bb, cc);
				bb.clear();
				if (obj.getClass().isArray()) {
					Class<?> component = classinfo.getClass().getComponentType();
					int length = Array.getLength(obj);
					Object r = Array.newInstance(component, length);
					for (int i = 0; i < length; i++)
						Array.set(r, i, component.cast(Array.get(obj, i)));
					cache.put((K) key, (V) r);
				} else
					cache.put((K) key, (V) obj);
			} catch (IOException e) {
				logger.error("kraken logdb: get error", e);
			}
		}

		return cache.get(key);
	}

	@Override
	public V put(K key, V value) {
		if (classinfo == null && value != null)
			classinfo = value;

		V prev = cache.get(key);
		cache.put(key, value);
		if (fp.containsKey(key) && fp.get(key) != null)
			freeBlock.add(fp.get(key));
		fp.put(key, null);

		if (cache.size() > cacheSize)
			flush();

		return prev;
	}

	public void flush() {
		long offset = rafLength;
		int pos = 0;

		for (K key : cache.keySet()) {
			V value = cache.get(key);
			try {
				EncodingRule.encode(bb, value, cc);
			} catch (BufferOverflowException e) {
				write(offset, pos);
				offset += pos;
				pos = 0;
				bb.clear();
				EncodingRule.encode(bb, value, cc);
			}
			fp.put(key, new Block(offset + pos, bb.position() - pos));
			pos = bb.position();
		}
		write(offset, pos);

		bb.clear();
		cache.clear();
	}

	private void write(long offset, int length) {
		try {
			raf.seek(offset);
			raf.write(bb.array(), 0, length);
			rafLength += length;
		} catch (IOException e) {
			logger.error("kraken logdb: flush error", e);
		}
	}

	@Override
	public V remove(Object key) {
		V prev = cache.remove(key);
		if (fp.containsKey(key)) {
			Block b = fp.remove(key);
			if (b != null)
				freeBlock.add(b);
		}

		return prev;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (K key : m.keySet())
			put(key, m.get(key));
	}

	@Override
	public void clear() {
		cache.clear();
		fp.clear();
		freeBlock.clear();
		try {
			raf.seek(0);
		} catch (IOException e) {
		}
	}

	@Override
	public Set<K> keySet() {
		if (fp == null) {
			logger.debug("kraken logdb: file buffer map is already closed.");
			return new HashSet<K>();
		}
		return fp.keySet();
	}

	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	public void close() {
		try {
			raf.close();
		} catch (IOException e) {
		}
		cache = null;
		fp = null;
		freeBlock = null;
		file.delete();
	}

	private class Block {
		private long fp;
		private int size;

		private Block(long fp, int size) {
			this.fp = fp;
			this.size = size;
		}
	}

	private class BlockComparator implements Comparator<Block> {
		@Override
		public int compare(Block o1, Block o2) {
			return o2.size - o1.size;
		}
	}
}
