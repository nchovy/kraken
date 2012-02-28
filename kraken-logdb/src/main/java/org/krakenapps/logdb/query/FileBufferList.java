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
import java.lang.ref.WeakReference;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.krakenapps.codec.CustomCodec;
import org.krakenapps.codec.EncodingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBufferList<E> implements List<E> {
	private static File BASE_DIR = new File(".");
	private static int BYTEBUFFER_CAPACITY = 1048576; // 1MB
	private Logger logger = LoggerFactory.getLogger(FileBufferList.class);

	private Queue<E> cache = new ConcurrentLinkedQueue<E>();
	private int cacheLimit;
	private List<FlipedObject> fliped;
	private AtomicInteger size = new AtomicInteger();
	private int flushed = 0;

	private File file;
	private RandomAccessFile raf;
	private Object fileLock = new Object();
	private List<ReadBuffer> readBuffers = new ArrayList<ReadBuffer>();

	private Comparator<E> comparator;
	private CustomCodec cc;

	public static void setFileDir(File dir) {
		BASE_DIR = dir;
	}

	public FileBufferList() throws IOException {
		this(50000, null);
	}

	public FileBufferList(int cacheLimit) throws IOException {
		this(cacheLimit, null);
	}

	public FileBufferList(Comparator<E> comparator) throws IOException {
		this(50000, comparator);
	}

	public FileBufferList(CustomCodec cc) throws IOException {
		this(50000, null, cc);
	}

	public FileBufferList(int cacheLimit, Comparator<E> comparator) throws IOException {
		this(cacheLimit, comparator, null);
	}

	public FileBufferList(Comparator<E> comparator, CustomCodec cc) throws IOException {
		this(50000, comparator, cc);
	}

	public FileBufferList(int cacheLimit, Comparator<E> comparator, CustomCodec cc) throws IOException {
		if (!BASE_DIR.exists())
			BASE_DIR.mkdirs();

		this.cacheLimit = cacheLimit;
		this.fliped = new ArrayList<FlipedObject>(cacheLimit);

		this.file = File.createTempFile("fbl", ".buf", BASE_DIR);
		this.file.deleteOnExit();
		this.raf = new RandomAccessFile(file, "rw");

		this.comparator = comparator;
		this.cc = cc;
	}

	@Override
	public boolean add(E obj) {
		cache.add(obj);

		if (size.incrementAndGet() % cacheLimit == 0) {
			try {
				flush();
			} catch (IOException e) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int size() {
		return size.get();
	}

	@Override
	public boolean isEmpty() {
		return (size.get() == 0);
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
		if (index < 0 || index >= size())
			throw new IndexOutOfBoundsException();

		try {
			flip();
		} catch (IOException e) {
			return null;
		}

		int l = 0;
		int r = fliped.size();
		while (l < r) {
			int m = (l + r) / 2;
			FlipedObject fo = fliped.get(m);
			if (fo.index == index)
				return fo.obj;
			else if (fo.index < index)
				l = m + 1;
			else
				r = m;
		}

		index -= l;
		ReadBuffer buf = null;

		l = 0;
		r = readBuffers.size();
		while (l < r) {
			int m = (l + r) / 2;
			buf = readBuffers.get(m);
			if (index < buf.offset)
				r = m;
			else if (index >= buf.offset + buf.count)
				l = m + 1;
			else
				break;
		}
		return buf.get(index - buf.offset);
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
		int size = size();
		if (fromIndex < 0 || fromIndex > size)
			throw new IndexOutOfBoundsException();
		if (toIndex < fromIndex || toIndex > size)
			throw new IndexOutOfBoundsException();

		try {
			flip();
		} catch (IOException e) {
			return null;
		}

		List<E> sub = new ArrayList<E>();
		for (int i = fromIndex; i < toIndex; i++)
			sub.add(get(i));
		return sub;
	}

	public synchronized void flush() throws IOException {
		flip();
		if (fliped.isEmpty())
			return;

		ByteBuffer bb = ByteBuffer.allocate(BYTEBUFFER_CAPACITY);
		int count = 0;
		int bufsize = 0;
		if (comparator == null) {
			for (FlipedObject fo : fliped) {
				try {
					EncodingRule.encode(bb, fo.obj, cc);
					bufsize = bb.position();
				} catch (BufferOverflowException e) {
					write(readBuffers, bb, count, bufsize, null);
					count = 0;
					EncodingRule.encode(bb, fo.obj, cc);
					bufsize = bb.position();
				}
				count++;
			}
			write(readBuffers, bb, count, bufsize, null);
		} else {
			E last = null;
			List<ReadBuffer> newBuffers = new ArrayList<ReadBuffer>();
			ListIterator<ReadBuffer> bufit = readBuffers.listIterator();
			ReadBuffer bnext = (bufit.hasNext()) ? bufit.next() : null;
			int bnextIndex = 0;

			Queue<byte[]> byteQueue = new LinkedList<byte[]>();

			Iterator<FlipedObject> flipit = fliped.iterator();
			FlipedObject fnext = flipit.next();
			int idx = 0;
			while (bnext != null) {
				idx = bnext.offset;
				if (bnext.offset <= fnext.index && fnext.index < bnext.offset + bnext.count)
					break;

				newBuffers.add(bnext);
				bnext = (bufit.hasNext()) ? bufit.next() : null;
			}

			for (; idx < flushed + fliped.size(); idx++) {
				if (fnext != null && idx == fnext.index) {
					try {
						EncodingRule.encode(bb, fnext.obj);
						bufsize = bb.position();
					} catch (BufferOverflowException e) {
						if (!byteQueue.isEmpty() || (bnext != null && bufit.nextIndex() - 1 <= newBuffers.size())) {
							byteQueue.add(Arrays.copyOf(bb.array(), BYTEBUFFER_CAPACITY));
							bb.clear();
							newBuffers.add(new ReadBuffer(newBuffers, count, bufsize, last));
						} else
							write(newBuffers, bb, count, bufsize, last);
						count = 0;
						EncodingRule.encode(bb, fnext.obj);
						bufsize = bb.position();
					}
					last = fnext.obj;
					count++;

					fnext = (flipit.hasNext()) ? flipit.next() : null;
				} else if (bnext != null) {
					int limit = Math.min((bnext.count - bnextIndex), (fnext != null) ? (fnext.index - idx) : Integer.MAX_VALUE);

					while (limit > 0) {
						int copied = bnext.getBytes(bb, bnextIndex, limit);
						bnextIndex += copied;
						count += copied;
						bufsize = bb.position();
						limit -= copied;
						idx += copied;
						if (copied > 0)
							last = bnext.get(bnextIndex - 1);

						if (limit > 0) {
							if (!byteQueue.isEmpty() || (bnext != null && bufit.nextIndex() - 1 <= newBuffers.size())) {
								byteQueue.add(Arrays.copyOf(bb.array(), BYTEBUFFER_CAPACITY));
								bb.clear();
								newBuffers.add(new ReadBuffer(newBuffers, count, bufsize, last));
							} else
								write(newBuffers, bb, count, bufsize, last);
							count = 0;
						}
					}
					idx--;

					if (bnextIndex == bnext.count) {
						bnextIndex = 0;
						if (bufit.hasNext()) {
							bnext = bufit.next();
							synchronized (fileLock) {
								while (bufit.nextIndex() - 1 > newBuffers.size() - byteQueue.size()) {
									raf.seek((long) (newBuffers.size() - byteQueue.size()) * (long) BYTEBUFFER_CAPACITY);
									raf.write(byteQueue.poll());
								}
							}
						} else {
							bnext = null;
							synchronized (fileLock) {
								while (!byteQueue.isEmpty()) {
									raf.seek((long) (newBuffers.size() - byteQueue.size()) * (long) BYTEBUFFER_CAPACITY);
									raf.write(byteQueue.poll());
								}
							}
						}
					}
				}
			}

			write(newBuffers, bb, count, bufsize, last);
			readBuffers = newBuffers;
		}
		flushed += fliped.size();
		fliped.clear();
	}

	private void write(List<ReadBuffer> buffers, ByteBuffer bb, int count, int bufsize, E last) throws IOException {
		synchronized (fileLock) {
			raf.seek((long) (buffers.size()) * (long) (BYTEBUFFER_CAPACITY));
			raf.write(bb.array());
		}
		bb.clear();
		buffers.add(new ReadBuffer(buffers, count, bufsize, last));
	}

	private synchronized void flip() throws IOException {
		if (cache.isEmpty())
			return;

		int index = flushed;
		while (!cache.isEmpty())
			fliped.add(new FlipedObject(index++, cache.poll()));

		if (comparator != null) {
			Collections.sort(fliped, new Comparator<FlipedObject>() {
				@Override
				public int compare(FlipedObject o1, FlipedObject o2) {
					return comparator.compare(o1.obj, o2.obj);
				}
			});

			Iterator<ReadBuffer> it = readBuffers.iterator();
			ReadBuffer buf = (it.hasNext()) ? it.next() : null;
			int offset = 0;
			int d = 0;
			for (FlipedObject fo : fliped) {
				E e = fo.obj;

				// find block
				while (buf != null) {
					E blockLast = buf.getLast();

					if (comparator.compare(e, blockLast) > 0) {
						if (it.hasNext()) {
							buf = it.next();
							offset = buf.offset;
						} else {
							offset = buf.offset + buf.count;
							buf = null;
						}
					} else
						break;
				}

				// find index
				if (buf != null) {
					int r = buf.offset + buf.count;
					while (offset < r) {
						int m = (offset + r) / 2;
						E blockObj = buf.get(m - buf.offset);
						int cmp = comparator.compare(e, blockObj);
						if (cmp > 0)
							offset = m + 1;
						else if (cmp < 0)
							r = m;
						else
							offset = r = m;
					}
				}

				fo.index = offset + (d++);
			}
		}
	}

	public void close() {
		try {
			cache = null;
			readBuffers = null;
			raf.close();
			file.delete();
		} catch (IOException e) {
		}
	}

	private class FlipedObject {
		private int index;
		private E obj;

		public FlipedObject(int index, E obj) {
			this.index = index;
			this.obj = obj;
		}
	}

	private class ReadBuffer {
		private int index;
		private int offset;
		private int count;
		private int bufsize;
		private WeakReference<ByteBuffer> buf;
		private WeakReference<int[]> pos;
		private int lastPos;
		private E last;

		public ReadBuffer(List<ReadBuffer> list, int count, int bufsize, E last) {
			this.index = list.size();
			this.offset = 0;
			if (!list.isEmpty()) {
				ReadBuffer lastbuf = list.get(list.size() - 1);
				offset = lastbuf.offset + lastbuf.count;
			}
			this.count = count;
			this.bufsize = bufsize;
			this.last = last;
		}

		public synchronized E get(int index) {
			if (index < 0 || count <= index)
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
			return decode(getBuffer(index));
		}

		public E getLast() {
			return last;
		}

		public synchronized int getBytes(ByteBuffer bb, int from, int limit) {
			if (from < 0 || count <= from || limit < 0 || count < from + limit)
				throw new IndexOutOfBoundsException();

			ByteBuffer buf = getBuffer(from);
			int pos = buf.position();
			int[] ps = setPosition(buf, from);

			int i;
			int copylen = 0;
			for (i = 0; i < limit; i++) {
				int idx = from + i;

				int len;
				if (idx == count - 1)
					len = bufsize - ps[idx];
				else {
					if (ps[idx + 1] == 0) {
						buf.position(ps[idx]);
						ps[idx + 1] = ps[idx] + EncodingRule.getObjectLength(buf, cc);
						lastPos++;
						buf.position(ps[idx + 1]);
					}
					len = ps[idx + 1] - ps[idx];
				}

				if (bb.remaining() < copylen + len)
					break;
				copylen += len;
			}

			System.arraycopy(buf.array(), pos, bb.array(), bb.position(), copylen);
			bb.position(bb.position() + copylen);

			return i;
		}

		private ByteBuffer getBuffer(int index) {
			ByteBuffer readBuffer = (buf != null) ? buf.get() : null;
			if (readBuffer == null) {
				readBuffer = ByteBuffer.allocate(BYTEBUFFER_CAPACITY);
				try {
					synchronized (fileLock) {
						raf.seek((long) (this.index) * (long) (BYTEBUFFER_CAPACITY));
						raf.readFully(readBuffer.array());
					}
				} catch (IOException e) {
					logger.error("kraken logdb: object read fail", e);
					return null;
				}
				buf = new WeakReference<ByteBuffer>(readBuffer);
			}

			if (index == 0)
				readBuffer.position(0);
			else
				setPosition(readBuffer, index);

			return readBuffer;
		}

		private int[] setPosition(ByteBuffer readBuffer, int index) {
			int[] ps = (pos != null) ? pos.get() : null;
			if (ps == null) {
				lastPos = 0;
				ps = new int[count];
				pos = new WeakReference<int[]>(ps);
			}

			readBuffer.position(ps[lastPos]);
			while (lastPos < index) {
				int p = readBuffer.position() + EncodingRule.getObjectLength(readBuffer, cc);
				ps[++lastPos] = p;
				readBuffer.position(p);
			}
			readBuffer.position(ps[index]);

			return ps;
		}

		@SuppressWarnings("unchecked")
		private E decode(ByteBuffer readBuffer) {
			return (E) EncodingRule.decode(readBuffer, cc);
		}
	}

	private class FileBufferListIterator implements ListIterator<E> {
		private int index;

		private FileBufferListIterator() {
			this(0);
		}

		private FileBufferListIterator(int index) {
			this.index = index;
		}

		@Override
		public boolean hasNext() {
			return (index < size.get());
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		Iterator<E> it = iterator();
		while (it.hasNext()) {
			sb.append(it.next().toString());
			if (it.hasNext())
				sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}
}
