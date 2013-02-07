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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.krakenapps.util.Managed;
import org.krakenapps.util.ManagedInstanceFactory;
import org.krakenapps.util.SingletonRegistry;
import org.krakenapps.util.directoryfile.exceptions.InvalidAbsPathException;
import org.krakenapps.util.directoryfile.exceptions.InvalidParameterexception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryFileArchive extends Managed {
	///////// BEGIN OF INSTANCE MANAGER
	public static DirectoryFileArchive open(String dirAbsPath) {
		DirectoryFileArchive directoryFileArchive = instanceRegistry.get(dirAbsPath);
		directoryFileArchive.refCount.incrementAndGet();
		return directoryFileArchive;
	}

	// @formatter:off
	private static class FactoryImpl implements ManagedInstanceFactory<DirectoryFileArchive, String> {
		@Override
		public DirectoryFileArchive newInstance() { return null;	}
		@Override
		public DirectoryFileArchive newInstance(String k) { return new DirectoryFileArchive(instanceRegistry, k); }
	}

	private static SingletonRegistry<String, DirectoryFileArchive> instanceRegistry; 
		
	static {
		instanceRegistry = new SingletonRegistry<String, DirectoryFileArchive>(new FactoryImpl());
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				SingletonRegistry<String, DirectoryFileArchive> guard = instanceRegistry;
				instanceRegistry = null;
				guard.closeAll();
			}
			}));
	}

	// @formatter:on
	///////// END OF INSTANCE MANAGER

	private static final String dataExtension = ".jdf";
	private static final String indexExtension = ".jdi";
	private ConcurrentSkipListMap<String, IndexEntry> index = new ConcurrentSkipListMap<String, IndexEntry>();
	private RandomAccessFile file = null;
	private String dirAbsPath;
	private IndexEntry rootIndexEntry;

	private AtomicInteger refCount = new AtomicInteger(0);

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	static enum IndexEntryType {
		FILE, DIRECTORY
	}

	// @formatter:off
	static class IndexEntry {
		public IndexEntryType type;
		public long startPos;
		public long lastModified;
		public long size;
		public long reserved;

		public IndexEntry(IndexEntryType type, long startPos, long size) {
			this.type = type; this.startPos = startPos; this.size = size; 
			this.lastModified = new Date().getTime();
		}

		public IndexEntry(IndexEntryType type, long startPos, long size, long lastModified) {
			this.type = type; this.startPos = startPos; this.size = size; 
			this.lastModified = lastModified;
		}
		
		public IndexEntry(IndexEntryType type, long startPos, long size, long lastModified, long reserved) {
			this.type = type; this.startPos = startPos; this.size = size; 
			this.lastModified = lastModified;
			this.reserved = reserved;
		}
	}
	// @formatter:on

	private DirectoryFileArchive(SingletonRegistry<String, DirectoryFileArchive> ir, String dirFullPath) {
		super(ir, dirFullPath);
		dirAbsPath = normalizePathSeparator(new File(dirFullPath).getAbsolutePath());
		logger.trace("new DirectoryFileArchive instance for " + dirAbsPath);
		if (new File(dirAbsPath).exists())
			load();
	}

	private void load() {
		logger.trace("DirectoryFileArchive load for " + dirAbsPath);
		String filenameBase = chooseFilename(dirAbsPath);
		try {
			File parent = new File(dirAbsPath);
			parent.mkdirs();
			file = new RandomAccessFile(new File(parent, filenameBase + dataExtension), "rw");
		} catch (FileNotFoundException e) {
		}

		index = new ConcurrentSkipListMap<String, IndexEntry>();
		if (loadIndex(dirAbsPath, index)) {
			rootIndexEntry = index.get("");
		} else {
			rootIndexEntry = new IndexEntry(IndexEntryType.DIRECTORY, 0, 0);
			index.put("", rootIndexEntry);
		}
	}

	private synchronized void writeIndex() throws FileNotFoundException, IOException {
		if (file == null)
			return;
		if (index.entrySet().isEmpty())
			return;

		String filenameBase = chooseFilename(dirAbsPath);
		ObjectOutputStream oos = null;
		try {
			new File(dirAbsPath).mkdirs();
			oos = new ObjectOutputStream(new FileOutputStream(new File(dirAbsPath, filenameBase + indexExtension)));
			oos.writeInt(index.size());
			for (Entry<String, IndexEntry> entry : index.entrySet()) {
				oos.writeUTF(entry.getKey());
				IndexEntry indexEntry = entry.getValue();
				oos.writeByte(indexEntry.type.ordinal());
				oos.writeLong(indexEntry.startPos);
				oos.writeLong(indexEntry.size);
				oos.writeLong(indexEntry.lastModified);
				oos.writeLong(indexEntry.reserved);
			}
			oos.close();
		} finally {
			if (oos != null)
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public static synchronized boolean loadIndex(String dirAbsPath, ConcurrentMap<String, IndexEntry> index) {
		String filenameBase = chooseFilename(dirAbsPath);
		ObjectInputStream ois = null;
		try {
			FileInputStream fis = new FileInputStream(new File(dirAbsPath, filenameBase + indexExtension));
//			index = new ConcurrentSkipListMap<String, IndexEntry>();
			ois = new ObjectInputStream(fis);
			int indexSize = ois.readInt();
			for (int i = 0; i < indexSize; ++i) {
				String key = ois.readUTF();
				byte type = ois.readByte();
				long startPos = ois.readLong();
				long size = ois.readLong();
				long lastModified = ois.readLong();
				long reserved = ois.readLong();
				index.put(key, new IndexEntry(getIndexEntryTypeFromOrdinal(type), startPos, size, lastModified,
						reserved));
			}
			return true;
		} catch (IOException e) {
//			index = new ConcurrentSkipListMap<String, IndexEntry>();
			return false;
		} finally {
			if (ois != null)
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private static IndexEntryType getIndexEntryTypeFromOrdinal(byte type) {
		return type == IndexEntryType.FILE.ordinal() ? IndexEntryType.FILE : IndexEntryType.DIRECTORY;
	}

	public long getReservedAbsolutePath(String absPath) throws IOException {
		String subPath = extractSubPath(normalizePathSeparator(absPath));
		return getReserved(subPath);
	}

	public void setReservedAbsolutePath(String absPath, long value) throws IOException {
		String subPath = extractSubPath(normalizePathSeparator(absPath));
		setReserved(subPath, value);
	}

	public long getReserved(String subPath) throws FileNotFoundException {
		IndexEntry indexEntry = index.get(subPath);
		if (indexEntry == null)
			throw new FileNotFoundException(subPath);
		else
			return index.get(subPath).reserved;
	}

	public void setReserved(String subPath, long value) throws FileNotFoundException {
		IndexEntry indexEntry = index.get(subPath);
		if (indexEntry != null)
			indexEntry.reserved = value;
		else
			throw new FileNotFoundException(subPath);
	}

	private static String chooseFilename(String dirFullPath) {
		String result = new File(dirFullPath).getName();
		if (result.isEmpty())
			return "Root";
		else
			return result;
	}

	public DirectoryFileOutputStream getOutputStreamAbsolutePath(String absPath, int size)
			throws InvalidAbsPathException,
			InvalidParameterexception, IOException {
		absPath = normalizePathSeparator(absPath);
		String subpath = extractSubPath(absPath);

		return getOutputStream(subpath, size);
	}

	private String extractSubPath(String normalizedAbsPath) throws IOException {
		if (!normalizedAbsPath.startsWith(dirAbsPath)) {
			throw new InvalidAbsPathException();
		}
		String subpath = normalizedAbsPath.substring(dirAbsPath.length());
		if (subpath.length() == 0) {
			if (normalizedAbsPath.length() == dirAbsPath.length())
				return "/";
			throw new InvalidAbsPathException();
		} else if (subpath.endsWith("/"))
			throw new InvalidParameterexception(subpath);

		return subpath;
	}

	private static String normalizePathSeparator(String path) {
		return path.replace(File.separatorChar, '/');
	}

	public DirectoryFileOutputStream getOutputStream(String subPath, long size) throws IOException {
		subPath = normalizePathSeparator(subPath);
		if (!subPath.startsWith("/")) {
			subPath = "/" + subPath;
		}

		return _getOutputStream(subPath, size);
	}

	private DirectoryFileOutputStream _getOutputStream(String subPath, long size) throws IOException {
		if (file == null)
			load();

		IndexEntry entry = null;
		if (index.containsKey(subPath)) {
			entry = index.get(subPath);
			if (entry.size < size) {
				index.remove(subPath);
				return new DirectoryFileOutputStream(this, newReadWriteMappedByteBuffer(subPath, size), subPath);
			}
			// read
			FileChannel channel = file.getChannel();
			MappedByteBuffer map = channel.map(MapMode.READ_WRITE, entry.startPos, entry.size);
			entry.lastModified = new Date().getTime();
			return new DirectoryFileOutputStream(this, map, subPath);
		} else {
			try {
				return new DirectoryFileOutputStream(this, newReadWriteMappedByteBuffer(subPath, size), subPath);
			} catch (IOException e) {
				return null;
			}
		}
	}

	private synchronized MappedByteBuffer newReadWriteMappedByteBuffer(String subPath, long size) throws IOException {
		IndexEntry entry = null;
		FileChannel channel = file.getChannel();
		if (index.containsKey(subPath)) {
			entry = index.get(subPath);
			return channel.map(MapMode.READ_WRITE, entry.startPos, entry.size);
		} else {
			long startPos = channel.size();
			entry = putToIndex(subPath, new IndexEntry(IndexEntryType.FILE, startPos, size));
			if (entry != null) {
				MappedByteBuffer map = channel.map(MapMode.READ_WRITE, entry.startPos, entry.size);
				return map;
			} else {
				return null;
			}
		}
	}

	private IndexEntry putToIndex(String subPath, IndexEntry indexEntry) {
		if (!subPath.isEmpty()) {
			String parent = getParent(subPath);
			IndexEntry parentEntry = index.get(parent);
			if (parentEntry == null) { // parent IndexEntry not exists
				// try to add parent
				IndexEntry putResult = putToIndex(parent, new IndexEntry(IndexEntryType.DIRECTORY, 0, 0));
				if (putResult != null) {
					index.put(subPath, indexEntry);
					return indexEntry;
				} else {
					return null;
				}
			} else { // parent exists
				if (parentEntry.type == IndexEntryType.FILE) { // if parent is FILE, discard it
					return null;
				} else {
					index.put(subPath, indexEntry);
					parentEntry.lastModified = new Date().getTime(); // update last Modified
					return indexEntry;
				}
			}
		} else {
			return rootIndexEntry;
		}
	}

	private String getParent(String subPath) {
		int lastSlashIndex = subPath.lastIndexOf('/');
		if (lastSlashIndex == -1)
			return "";
		else
			return subPath.substring(0, lastSlashIndex);
	}

	public boolean exists(File file) {
		try {
			String absPath = normalizePathSeparator(file.getAbsolutePath());
			String subPath = extractSubPath(absPath);
			return index.containsKey(subPath);
		} catch (IOException e) {
			return false;
		}
	}

	public DirectoryFileInputStream getInputStreamAbsolutePath(String absPath) throws IOException {
		absPath = normalizePathSeparator(absPath);
		String subPath = extractSubPath(absPath);

		return getInputStream(subPath);
	}

	public DirectoryFileInputStream getInputStream(String subPath) throws IOException {
		subPath = normalizePathSeparator(subPath);
		if (!subPath.startsWith("/")) {
			subPath = "/" + subPath;
		}

		return _getInputStream(subPath);
	}

	private DirectoryFileInputStream _getInputStream(String subPath) throws IOException {
		if (!index.containsKey(subPath))
			return null;
		if (file == null) {
			if (!new File(dirAbsPath).exists())
				return null;
		}

		IndexEntry entry = index.get(subPath);

		FileChannel channel = file.getChannel();
		MappedByteBuffer map = channel.map(MapMode.PRIVATE, entry.startPos, entry.size);

		return new DirectoryFileInputStream(this, map, subPath);
	}

	public long getLastModified(String subPath) {
		subPath = normalizePathSeparator(subPath);
		if (!subPath.startsWith("/")) {
			subPath = "/" + subPath;
		}

		if (index.containsKey(subPath)) {
			return index.get(subPath).lastModified;
		} else {
			return -1;
		}
	}

	public void attach() {
		refCount.incrementAndGet();
	}

	@Override
	public void close() throws IOException {
		logger.debug("closing archive: {}, RefCount: {}", this.dirAbsPath, this.refCount.get());
		if (refCount.decrementAndGet() == 0) {
			logger.debug("close requested");
			super.close();
		}
	}

	public String getSubPath(File file) throws IOException {
		return extractSubPath(normalizePathSeparator(file.getAbsolutePath()));
	}

	public String getDirAbsPath() {
		return dirAbsPath;
	}

	private String extractName(String normalizedPath) {
		return normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1);
	}
	
	public String[] getSubMapExtractor(String subPath) {
		if (subPath.equals("/")) {
			return new String[] { subPath, "0" };
		} else {
			return new String[] { subPath + "/", subPath + "0" };
		}		
	}
	
	public List<String> getChildren(String subPath) {
		return getChildren(subPath, (FileFilter) null);
	}
		
	public List<String> getChildren(String subPath, FileFilter filter) {
		subPath = normalizePathSeparator(subPath);
		if (!subPath.startsWith("/")) {
			subPath = "/" + subPath;
		}

		String[] range = getSubMapExtractor(subPath);
		ConcurrentNavigableMap<String, IndexEntry> subMap = index.subMap(range[0], range[1]);
		int subPathStart = subPath.length() + 1;
		List<String> result = new ArrayList<String>(subMap.size());
		for (Entry<String, IndexEntry> entry: subMap.entrySet()) {
			// filter immediate child
			if (entry.getKey().indexOf("/", subPathStart + 1) != -1)
				continue;
			if (filter == null || filter.accept(new File(dirAbsPath, entry.getKey()))) {
				result.add(entry.getKey().substring(subPath.length()));
			}
		}
		return result;
	}
	
	public List<String> getChildren(String subPath, FilenameFilter filter) {
		subPath = normalizePathSeparator(subPath);
		if (!subPath.startsWith("/")) {
			subPath = "/" + subPath;
		}

		String[] range = getSubMapExtractor(subPath);
		ConcurrentNavigableMap<String, IndexEntry> subMap = index.subMap(range[0], range[1]);
		int subPathStart = subPath.length() + 1;
		List<String> result = new ArrayList<String>(subMap.size());
		for (Entry<String, IndexEntry> entry: subMap.entrySet()) {
			// filter immediate child
			if (entry.getKey().indexOf("/", subPathStart + 1) != -1)
				continue;
			String bn = extractName(entry.getKey());
			if (filter == null || filter.accept(new File(dirAbsPath, subPath), bn)) {
				result.add(bn);
			}
		}
		return result;
	}

	public List<String> getDescendants(String subPath, FileFilter filter) {
		subPath = normalizePathSeparator(subPath);
		if (!subPath.startsWith("/")) {
			subPath = "/" + subPath;
		}

		String[] range = getSubMapExtractor(subPath);
		ConcurrentNavigableMap<String, IndexEntry> subMap = index.subMap(range[0], range[1]);
		List<String> result = new ArrayList<String>(subMap.size());
		for (Entry<String, IndexEntry> entry : subMap.entrySet()) {
			if (filter == null || filter.accept(new File(dirAbsPath, entry.getKey()))) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	public List<String> getDescendants(String subPath, FilenameFilter filter) {
		subPath = normalizePathSeparator(subPath);
		if (!subPath.startsWith("/")) {
			subPath = "/" + subPath;
		}

		String[] range = getSubMapExtractor(subPath);
		ConcurrentNavigableMap<String, IndexEntry> subMap = index.subMap(range[0], range[1]);
		List<String> result = new ArrayList<String>(subMap.size());
		for (Entry<String, IndexEntry> entry : subMap.entrySet()) {
			String bn = extractName(entry.getKey());
			if (filter == null || filter.accept(new File(dirAbsPath, subPath), bn)) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	public synchronized void sync() throws FileNotFoundException, IOException {
		writeIndex();
	}

	@Override
	protected void onClose() throws IOException {
		try {
			writeIndex();
		} finally {
			if (file != null)
				file.close();
		}
	}

	@Override
	protected void errorOnClosing(Throwable e) {
		logger.warn("Error occurred while closing directory file archive", e);
	}

	public void setActualSize(String subPath, int actualSize) {
		IndexEntry ie = index.get(subPath);
		if (ie == null)
			throw new IllegalStateException();
		ie.size = actualSize;
	}

	public long getActualSize(String subPath) {
		IndexEntry ie = index.get(subPath);
		if (ie == null)
			throw new IllegalStateException();
		return ie.size;		
	}
}
