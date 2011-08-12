package org.krakenapps.logstorage.query;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;
import java.util.Queue;

import org.krakenapps.codec.CustomCodec;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.codec.UnsupportedTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBufferList<E> implements List<E> {
	private static File BASE_DIR = new File(System.getProperty("kraken.data.dir"), "kraken-logstorage/query/");
	private static int BYTEBUFFER_CAPACITY = 655360; // 640KB
	private Logger logger = LoggerFactory.getLogger(FileBufferList.class);
	private List<E> cache = new ArrayList<E>();
	private int cacheSize;

	private File file;
	private RandomAccessFile raf;
	private Comparator<E> comparator;
	private volatile int size = 0;
	private ByteBuffer bb = ByteBuffer.allocate(BYTEBUFFER_CAPACITY);
	private CustomCodec cc;

	private boolean flip = false;
	private List<Long> fp = new ArrayList<Long>();

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
		this.raf = new RandomAccessFile(file, "rw");
		this.cacheSize = cacheSize;
		this.cc = cc;
	}

	@Override
	public boolean add(E obj) {
		if (flip)
			flip = false;

		size++;
		cache.add(obj);
		if (cache.size() >= cacheSize) {
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

	@SuppressWarnings("unchecked")
	@Override
	public E get(int index) {
		if (!flip) {
			try {
				flip();
			} catch (IOException e) {
			}
		}

		long fp = this.fp.get(index);
		try {
			raf.seek(fp);
			raf.read(bb.array());
			E e = (E) EncodingRule.decode(bb, cc);
			bb.clear();
			return e;
		} catch (UnsupportedTypeException e) {
			logger.error("kraken logstorage: invalid access file {}, index {}", file.getName(), index);
		} catch (IOException e) {
		}
		return null;
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
		throw new UnsupportedOperationException();
	}

	private void flush() throws IOException {
		if (comparator != null)
			Collections.sort(cache, comparator);

		long startFp = raf.getFilePointer();
		raf.write(getBytes(0));
		long currentFp = startFp + 4;

		int length = 0;
		int objectSize = 0;
		for (Object obj : cache) {
			if (comparator == null)
				fp.add(currentFp);

			int pos = bb.position();
			try {
				EncodingRule.encode(bb, obj, cc);
				objectSize = bb.position() - pos;
			} catch (BufferOverflowException e) {
				length += pos;
				raf.write(bb.array(), 0, pos);
				bb.clear();
				EncodingRule.encode(bb, obj, cc);
				objectSize = bb.position();
			}
			currentFp += objectSize;
		}
		cache.clear();

		raf.write(bb.array(), 0, bb.position());
		length += bb.position();
		bb.clear();

		raf.seek(startFp);
		raf.write(getBytes(length));
		raf.seek(raf.length());
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
		flip = true;

		if (!cache.isEmpty())
			flush();

		if (comparator == null)
			return;

		List<BlockReader> blockReaders = getBlockReaders();
		PriorityQueue<BlockObject> next = null;
		fp.clear();

		if (blockReaders.size() == 0)
			return;

		if (next == null) { // init
			next = new PriorityQueue<BlockObject>(blockReaders.size(), new BlockObjectComparator(comparator));
			for (int i = 0; i < blockReaders.size(); i++) {
				try {
					BlockReader br = blockReaders.get(i);
					next.add(br.read());
				} catch (EOFException e) {
				}
			}
		}

		while (!next.isEmpty()) {
			BlockObject bo = next.poll();
			fp.add(bo.fp);
			try {
				BlockReader br = blockReaders.get(bo.blockNum);
				next.add(br.read());
			} catch (EOFException e) {
			}
		}
	}

	public void close() {
		try {
			raf.close();
		} catch (IOException e) {
		}
		cache = null;
		bb = null;
		fp = null;
		file.delete();
	}

	private class BlockObject {
		private int blockNum;
		private long fp;
		private E obj;

		private BlockObject(int blockNum, long fp, E obj) {
			this.blockNum = blockNum;
			this.fp = fp;
			this.obj = obj;
		}
	}

	private class BlockObjectComparator implements Comparator<BlockObject> {
		private Comparator<E> comparator;

		public BlockObjectComparator(Comparator<E> comparator) {
			this.comparator = comparator;
		}

		@Override
		public int compare(BlockObject o1, BlockObject o2) {
			return comparator.compare(o1.obj, o2.obj);
		}
	}

	private List<BlockReader> getBlockReaders() throws IOException {
		List<BlockReader> readers = new ArrayList<BlockReader>();

		long offset = 0;
		while (offset < raf.length()) {
			raf.seek(offset);
			BlockReader br = new BlockReader(readers.size(), raf);
			readers.add(br);
			offset += br.limit;
		}

		return readers;
	}

	private class BlockReader {
		private int id;
		private int BLOCK_HEADER_SIZE = 4;
		private RandomAccessFile f;
		private long offset;
		private int limit;
		private int readed;
		private Queue<E> q = new LinkedList<E>();
		private Queue<Long> fp = new LinkedList<Long>();

		private BlockReader(int id, RandomAccessFile f) throws IOException {
			this.id = id;
			this.f = f;
			this.offset = f.getFilePointer();
			this.limit = f.readInt() + BLOCK_HEADER_SIZE;
			this.readed = BLOCK_HEADER_SIZE;
		}

		@SuppressWarnings("unchecked")
		public BlockObject read() throws IOException {
			if (!q.isEmpty())
				return new BlockObject(id, fp.poll(), q.poll());

			if (readed >= limit)
				throw new EOFException();

			f.seek(offset + readed);
			f.read(bb.array());
			if (limit - readed < bb.capacity())
				bb.limit(limit - readed);

			int pos = 0;
			while (bb.hasRemaining()) {
				try {
					long p = offset + readed + bb.position();
					q.add((E) EncodingRule.decode(bb, cc));
					fp.add(p);
				} catch (Exception e) {
					break;
				}
				pos = bb.position();
			}
			bb.clear();
			readed += pos;

			return new BlockObject(id, fp.poll(), q.poll());
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
			return get(index - 1);
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
