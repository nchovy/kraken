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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.SoftReference;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.krakenapps.codec.CustomCodec;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.codec.UnsupportedTypeException;
import org.krakenapps.logdb.query.FileBufferList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class FileBufferList<E> implements List<E> {
	private static File BASE_DIR = new File(System.getProperty("kraken.data.dir"), "kraken-logdb/query/");
	private static int BYTEBUFFER_CAPACITY = 655360; // 640KB
	private final Logger logger = LoggerFactory.getLogger(FileBufferList.class);
	private List<ObjectWrapper> cache = new ArrayList<ObjectWrapper>();
	private int cacheSize;
	private boolean needFlip = false;
	private int fliped = 0;
	private Object cacheLock = new Object();
	private Object readLock = new Object();

	private File file;
	private RandomAccessFile raf;
	private Object fileLock = new Object();
	private Comparator<E> comparator;
	private Comparator<ObjectWrapper> wrapperComparator;
	private volatile int size = 0;
	private ByteBuffer writeBuffer = ByteBuffer.allocate(BYTEBUFFER_CAPACITY);
	private ByteBuffer readBuffer = ByteBuffer.allocate(BYTEBUFFER_CAPACITY);
	private Long readBufferIndex;
	private CustomCodec cc;

	private List<ObjectWrapper> objs = new ArrayList<ObjectWrapper>();

	public FileBufferList() throws IOException {
		this(50000, null);
	}

	public FileBufferList(int cacheSize) throws IOException {
		this(cacheSize, null);
	}

	public FileBufferList(Comparator<E> comparator) throws IOException {
		this(50000, comparator);
	}

	public FileBufferList(CustomCodec cc) throws IOException {
		this(50000, null, cc);
	}

	public FileBufferList(int cacheSize, Comparator<E> comparator) throws IOException {
		this(cacheSize, comparator, null);
	}

	public FileBufferList(Comparator<E> comparator, CustomCodec cc) throws IOException {
		this(50000, comparator, cc);
	}

	public FileBufferList(int cacheSize, Comparator<E> comparator, CustomCodec cc) throws IOException {
		if (!BASE_DIR.exists())
			BASE_DIR.mkdirs();
		this.file = File.createTempFile("fbl", ".buf", BASE_DIR);
		this.file.deleteOnExit();
		this.comparator = comparator;
		if (comparator != null)
			this.wrapperComparator = new ObjectWrapperComparator(comparator);
		this.raf = new RandomAccessFile(file, "rw");
		this.cacheSize = cacheSize;
		this.cc = cc;
	}

	@Override
	public boolean add(E obj) {
		synchronized (cacheLock) {
			cache.add(new ObjectWrapper(obj));
		}
		needFlip = true;
		if (cache.size() >= cacheSize) {
			try {
				flush();
			} catch (IOException e) {
				return false;
			}
		}
		size++;

		return true;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<E> iterator() {
		return new FileBufferListIterator();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		for (E e : c) {
			if (!add(e))
				return false;
		}
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public E get(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();

		if (needFlip) {
			try {
				flip();
			} catch (IOException e) {
			}
		}

		ObjectWrapper op = this.objs.get(index);
		if (op == null)
			return null;
		return op.get();
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<E> listIterator() {
		return new FileBufferListIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new FileBufferListIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		if (fromIndex < 0 || fromIndex > size)
			throw new IndexOutOfBoundsException();
		if (toIndex < fromIndex || toIndex > size)
			throw new IndexOutOfBoundsException();

		if (needFlip) {
			try {
				flip();
			} catch (IOException e) {
			}
		}

		List<E> sub = new ArrayList<E>();
		for (int i = fromIndex; i < toIndex; i++)
			sub.add(objs.get(i).get());
		return sub;
	}

	private void flush() throws IOException {
		if (cache.isEmpty())
			return;

		if (needFlip)
			flip();

		List<ObjectWrapper> c = cache;
		cache = new ArrayList<ObjectWrapper>();
		fliped = 0;

		int beforeSize = c.size();
		if (wrapperComparator != null)
			beforeSize = sort(c);

		long startFp = raf.length();
		synchronized (fileLock) {
			raf.seek(startFp);
			raf.write(getBytes(0));
		}

		long currentFp = startFp + 4;
		int length = 0;
		int objectSize = 0;
		Iterator<ObjectWrapper> it = c.iterator();
		Map<ObjectWrapper, Long> flush = new HashMap<ObjectWrapper, Long>();
		for (int i = 0; i < beforeSize; i++) {
			ObjectWrapper ow = it.next();
			Object obj = ow.get();
			flush.put(ow, currentFp);

			int pos = writeBuffer.position();
			try {
				EncodingRule.encode(writeBuffer, obj, cc);
				objectSize = writeBuffer.position() - pos;
			} catch (BufferOverflowException e) {
				length += pos;
				synchronized (fileLock) {
					raf.seek(raf.length());
					raf.write(writeBuffer.array(), 0, pos);
				}
				writeBuffer.clear();
				EncodingRule.encode(writeBuffer, obj, cc);
				objectSize = writeBuffer.position();
			}
			currentFp += objectSize;
		}

		synchronized (fileLock) {
			raf.seek(raf.length());
			raf.write(writeBuffer.array(), 0, writeBuffer.position());

			raf.seek(startFp);
			raf.write(getBytes(length));
			raf.seek(raf.length());
		}
		length += writeBuffer.position();
		writeBuffer.clear();
		for (ObjectWrapper ow : flush.keySet())
			ow.flush(flush.get(ow));

		ListIterator<ObjectWrapper> li = c.listIterator(beforeSize);
		synchronized (cacheLock) {
			while (li.hasNext())
				cache.add(li.next());
		}
	}

	private int sort(List<ObjectWrapper> list) {
		return sort(list, 0);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int sort(List<ObjectWrapper> list, int offset) {
		Object[] a = list.toArray();
		Arrays.sort(a, offset, a.length, (Comparator) wrapperComparator);
		ListIterator i = list.listIterator(offset);
		for (int j = offset; j < a.length; j++) {
			i.next();
			i.set(a[j]);
		}
		return a.length;
	}

	private byte[] getBytes(int value) {
		byte[] b = new byte[4];
		for (int i = 3; i >= 0; i--) {
			b[i] = (byte) (value & 0xff);
			value >>= 8;
		}
		return b;
	}

	private void flip() throws IOException {
		needFlip = false;

		if (wrapperComparator == null) {
			synchronized (cacheLock) {
				Iterator<ObjectWrapper> it = cache.listIterator(fliped);
				while (it.hasNext()) {
					ObjectWrapper ow = it.next();
					objs.add(ow);
					fliped++;
				}
			}
		} else {
			int sorted = sort(cache, fliped);
			ListIterator<ObjectWrapper> li = cache.listIterator(fliped);
			int m = 0;
			List<ObjectWrapper> newObjs = new ArrayList<ObjectWrapper>(objs.size() + (sorted - fliped));
			int lastIndex = 0;
			for (int i = fliped; i < sorted; i++) {
				ObjectWrapper ow = li.next();
				E e1 = ow.get();
				int l = m;
				int r = objs.size();
				while (l < r) {
					m = (l + r) / 2;
					E e2 = objs.get(m).get();
					int comp = comparator.compare(e1, e2);
					if (comp < 0)
						r = m;
					else if (comp > 0)
						l = m + 1;
					else
						break;
				}
				for (; lastIndex < m; lastIndex++)
					newObjs.add(objs.get(lastIndex));
				newObjs.add(ow);
			}
			for (; lastIndex < objs.size(); lastIndex++)
				newObjs.add(objs.get(lastIndex));
			fliped += sorted;
			objs = newObjs;
		}
	}

	public void close() {
		try {
			cache = null;
			writeBuffer = null;
			readBuffer = null;
			objs = null;
			raf.close();
			file.delete();
		} catch (IOException e) {
		}
	}

	private class ObjectWrapper {
		private boolean isFlushed;
		private long fp;
		private SoftReference<E> cache;
		private E obj;

		public ObjectWrapper(E obj) {
			this.isFlushed = false;
			this.obj = obj;
		}

		public void flush(long fp) {
			this.isFlushed = true;
			this.fp = fp;
			this.obj = null;
		}

		public E get() {
			if (isFlushed) {
				E element = (cache != null) ? cache.get() : null;
				if (element == null) {
					synchronized (readLock) {
						if (readBufferIndex == null || readBufferIndex > fp || readBufferIndex + readBuffer.limit() <= fp)
							read(fp);

						try {
							return decode((int) (fp - readBufferIndex));
						} catch (IndexOutOfBoundsException e) {
							read(fp);
							return decode(0);
						} catch (UnsupportedTypeException e) {
							logger.error("kraken logdb: unsupported decode type, {}", e.getMessage());
						} catch (BufferUnderflowException e) {
							read(fp);
							return decode(0);
						} catch (IllegalArgumentException e) {
							read(fp);
							return decode(0);
						} catch (RuntimeException e) {
							logger.error("kraken logdb: cannot decode fp [{}], read index [{}] from file [{}]", new Object[] {
									fp, readBufferIndex, file.getAbsolutePath() });
							throw e;
						}
					}
				} else
					return element;
			} else {
				return obj;
			}

			return null;
		}

		private void read(long fp) {
			try {
				int readed = 0;
				synchronized (fileLock) {
					raf.seek(fp);
					readed = raf.read(readBuffer.array());
				}
				if (readed == -1)
					throw new EOFException();
				readBuffer.position(0);
				readBuffer.limit(readed);
				readBufferIndex = fp;
			} catch (IOException e) {
				logger.error("kraken logdb: invalid access file {}, fp {}", file.getName(), fp);
			} catch (RuntimeException e) {
				logger.error("kraken logdb: cannot read offset [{}] from file [{}]", fp, file.getAbsolutePath());
				throw e;
			}
		}

		@SuppressWarnings("unchecked")
		private E decode(int pos) {
			readBuffer.position(pos);
			E element = (E) EncodingRule.decode(readBuffer, cc);
			cache = new SoftReference<E>(element);
			return element;
		}
	}

	private class ObjectWrapperComparator implements Comparator<ObjectWrapper> {
		private Comparator<E> comparator;

		private ObjectWrapperComparator(Comparator<E> comparator) {
			this.comparator = comparator;
		}

		@Override
		public int compare(ObjectWrapper o1, ObjectWrapper o2) {
			return comparator.compare(o1.get(), o2.get());
		}
	}

	public class FileBufferListIterator implements ListIterator<E> {
		private int index;

		private FileBufferListIterator() {
			this(0);
		}

		private FileBufferListIterator(int index) {
			this.index = index;
		}

		@Override
		public boolean hasNext() {
			return (index < size);
		}

		@Override
		public E next() {
			return get(index++);
		}

		@Override
		public boolean hasPrevious() {
			return (index > 0);
		}

		@Override
		public E previous() {
			return get(--index);
		}

		@Override
		public int nextIndex() {
			return index + 1;
		}

		@Override
		public int previousIndex() {
			return index - 1;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(E e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(E e) {
			throw new UnsupportedOperationException();
		}
	}
}
