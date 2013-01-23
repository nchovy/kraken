/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.confdb.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantLock;

import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.api.PrimitiveSerializeCallback;
import org.krakenapps.confdb.CollectionEntry;
import org.krakenapps.confdb.CollectionName;
import org.krakenapps.confdb.CommitLog;
import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCache;
import org.krakenapps.confdb.ConfigChange;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigDatabaseListener;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.ConfigTransactionCache;
import org.krakenapps.confdb.Manifest;
import org.krakenapps.confdb.ManifestIterator;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.confdb.ReferenceKeys;
import org.krakenapps.confdb.RollbackException;
import org.krakenapps.confdb.WriteLockTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File config database instance should be only one in a JVM instance since it
 * uses file lock for exclusive write locking. In multi-threaded environment,
 * write lock is acquired using reentrant lock. If you create multiple
 * instances, write lock is not guaranteed.
 * 
 * @author xeraph
 * 
 */
public class FileConfigDatabase implements ConfigDatabase {
	private final Logger logger = LoggerFactory.getLogger(FileConfigDatabase.class);

	private final File baseDir;
	private final File dbDir;
	private final String dbName;

	/**
	 * base changeset revision
	 */
	private final Integer changeset;

	private final File changeLogFile;
	private final File changeDatFile;
	private final File manifestLogFile;
	private final File manifestDatFile;
	private final File counterFile;
	private final File lockFile;

	private final ReentrantLock threadLock;
	private FileLock processLock;

	/**
	 * default waiting transaction timeout in milliseconds
	 */
	private int defaultTimeout = 5000;

	// change set rev to manifest id cache
	private WeakReference<ConcurrentMap<Integer, Integer>> changeCache;

	// manifest id to manifest cache
	private WeakReference<ConcurrentMap<Integer, FileManifest>> manifestCache;

	// (collection id, manifest id) to snapshot cache
	private WeakReference<ConcurrentMap<SnapshotKey, List<RevLog>>> snapshotCache;

	// config cache
	private FileConfigCache configCache;

	private CopyOnWriteArraySet<ConfigDatabaseListener> listeners;

	public FileConfigDatabase(File baseDir, String name) throws IOException {
		this(baseDir, name, null);
	}

	public FileConfigDatabase(File baseDir, String name, Integer rev) throws IOException {
		this.baseDir = baseDir;
		this.dbName = name;
		this.dbDir = new File(baseDir, name);
		this.changeset = rev;
		this.threadLock = new ReentrantLock();
		this.changeCache = new WeakReference<ConcurrentMap<Integer, Integer>>(new ConcurrentHashMap<Integer, Integer>());
		this.manifestCache = new WeakReference<ConcurrentMap<Integer, FileManifest>>(
				new ConcurrentHashMap<Integer, FileManifest>());
		this.snapshotCache = new WeakReference<ConcurrentMap<SnapshotKey, List<RevLog>>>(
				new ConcurrentHashMap<SnapshotKey, List<RevLog>>());
		this.configCache = new FileConfigCache(this);

		changeLogFile = new File(dbDir, "changeset.log");
		changeDatFile = new File(dbDir, "changeset.dat");
		manifestLogFile = new File(dbDir, "manifest.log");
		manifestDatFile = new File(dbDir, "manifest.dat");
		lockFile = new File(dbDir, "write.lock");
		counterFile = new File(dbDir, "col.id");
		listeners = new CopyOnWriteArraySet<ConfigDatabaseListener>();
	}

	/**
	 * acquire write lock
	 */
	public void lock() {
		lock(defaultTimeout);
	}

	public void lock(int timeout) {
		// thread lock
		threadLock.lock();

		// process lock first
		Date begin = new Date();
		RandomAccessFile raf = null;
		FileChannel channel = null;
		try {
			lockFile.getParentFile().mkdirs();
			raf = new RandomAccessFile(lockFile, "rw");
			channel = raf.getChannel();

			while (processLock == null) {
				// check lock timeout
				Date now = new Date();
				if (now.getTime() - begin.getTime() > timeout)
					throw new WriteLockTimeoutException();

				processLock = channel.tryLock();
				if (processLock != null)
					break;
				Thread.sleep(100);
			}
		} catch (IOException e) {
			throw new IllegalStateException("cannot acquire write lock", e);
		} catch (InterruptedException e) {
			throw new IllegalStateException("cannot acquire write lock", e);
		} finally {
			// close channel if lock failed
			if (processLock == null && channel != null) {
				try {
					channel.close();
				} catch (IOException e1) {
				}
			}
		}

	}

	/**
	 * release write lock
	 */
	public void unlock() {
		// release process lock
		try {
			if (processLock != null) {
				processLock.release();
				processLock.channel().close();
				processLock = null;
			}
		} catch (IOException e) {
			throw new IllegalStateException("cannot release write lock", e);
		}

		// release thread lock
		threadLock.unlock();

	}

	@Override
	public String getName() {
		return dbName;
	}

	/**
	 * should be called in write locked context
	 * 
	 * @return the next collection id
	 */
	public int nextCollectionId() {
		RandomAccessFile raf = null;
		int next = 0;
		try {
			raf = new RandomAccessFile(counterFile, "rw");
			String line = raf.readLine();
			if (line != null)
				next = Integer.valueOf(line);

			next++;

			// cut off
			raf.setLength(0);

			// update counter
			raf.write((next + "\n").getBytes());

			logger.debug("kraken confdb: generated next collection id {}", next);
			return next;
		} catch (IOException e) {
			throw new IllegalStateException("cannot generate collection id", e);
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
				}
			}
		}
	}

	@Override
	public Set<String> getCollectionNames() {
		Manifest manifest = getManifest(changeset);
		return manifest.getCollectionNames();
	}

	@Override
	public ConfigCollection getCollection(Class<?> cls) {
		return getCollection(getCollectionName(cls));
	}

	public Integer getCollectionId(String name) {
		Manifest manifest = getManifest(changeset, true);
		CollectionEntry col = manifest.getCollectionEntry(name);
		if (col == null)
			return null;

		return col.getId();
	}

	@Override
	public ConfigCollection getCollection(String name) {
		try {
			Manifest manifest = getManifest(changeset, true);
			CollectionEntry col = manifest.getCollectionEntry(name);
			if (col == null) {
				logger.debug("kraken confdb: col [{}] not found", name);
				return null;
			}

			FileConfigCollection collection = new FileConfigCollection(this, changeset, col);
			if (changeset != null)
				return new UnmodifiableConfigCollection(collection);

			return collection;
		} catch (IOException e) {
			logger.error("kraken confdb: cannot open collection file", e);
			return null;
		}
	}

	@Override
	public ConfigCollection ensureCollection(Class<?> cls) {
		return ensureCollection(getCollectionName(cls));
	}

	@Override
	public ConfigCollection ensureCollection(String name) {
		return ensureCollection(null, name);
	}

	@Override
	public ConfigCollection ensureCollection(ConfigTransaction xact, Class<?> cls) {
		return ensureCollection(xact, getCollectionName(cls));
	}

	@Override
	public ConfigCollection ensureCollection(ConfigTransaction xact, String name) {
		try {
			Manifest manifest = null;
			if (xact == null) {
				manifest = getManifest(changeset, true);
			} else
				manifest = xact.getManifest();
			CollectionEntry col = manifest.getCollectionEntry(name);

			// create new collection if not exists
			if (col == null)
				col = createCollection(xact, name);

			FileConfigCollection collection = new FileConfigCollection(this, changeset, col);
			if (changeset != null)
				return new UnmodifiableConfigCollection(collection);

			return collection;
		} catch (IOException e) {
			logger.error("kraken confdb: cannot open collection file", e);
			return null;
		}
	}

	private CollectionEntry createCollection(ConfigTransaction xact, String name) throws IOException {
		boolean implicitTransact = xact == null;

		if (xact == null)
			xact = beginTransaction();

		try {
			// prevent duplicated collection name caused by race condition
			CollectionEntry col = xact.getManifest().getCollectionEntry(name);

			if (col != null) {
				logger.trace("kraken confdb: duplicated collection name [{}]", name);
				xact.rollback();
				return col;
			}

			xact.log(CommitOp.CreateCol, name, 0, 0, 0);
			if (implicitTransact) {
				xact.commit(null, null);
			}

			int newColId = xact.getManifest().getCollectionId(name);
			return new CollectionEntry(newColId, name);
		} catch (Throwable e) {
			xact.rollback();
			throw new RollbackException(e);
		}
	}

	@Override
	public void dropCollection(Class<?> cls) {
		dropCollection(getCollectionName(cls));
	}

	@Override
	public void dropCollection(String name) {
		ConfigTransaction xact = beginTransaction();

		try {
			CollectionEntry col = xact.getManifest().getCollectionEntry(name);
			if (col == null) {
				logger.trace("kraken confdb: does not exist collection name [{}]", name);
				xact.rollback();
				return;
			}

			xact.getManifest();
			xact.log(CommitOp.DropCol, name, 0, 0, 0);
			xact.commit(null, null);
		} catch (Throwable e) {
			xact.rollback();
			throw new RollbackException(e);
		}
	}

	private String getCollectionName(Class<?> cls) {
		CollectionName conf = cls.getAnnotation(CollectionName.class);
		return (conf == null) ? cls.getName() : conf.value();
	}

	public File getDbDirectory() {
		return new File(baseDir, dbName);
	}

	@Override
	public Manifest getManifest(Integer rev) {
		return getManifest(rev, false);
	}

	public Manifest getManifest(Integer rev, boolean noConfigs) {
		// read last changelog and get manifest doc id
		int manifestId = 0;
		RevLogReader reader = null;
		try {
			reader = new RevLogReader(changeLogFile, changeDatFile);
			RevLog revlog = null;

			if (rev == null) {
				long count = reader.count();
				revlog = reader.read(count - 1);
			} else {
				revlog = reader.findDoc(rev);
			}

			Integer cached = getCachedManifestId(revlog.getDocId());
			if (cached != null) {
				manifestId = cached;
			} else {
				byte[] doc = reader.readDoc(revlog.getDocOffset(), revlog.getDocLength());
				manifestId = ChangeLog.getManifest(doc);

				setChangeSetCache(revlog.getDocId(), manifestId);
			}
		} catch (FileNotFoundException e) {
			// changeset can be empty
			return new FileManifest();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			if (reader != null) {
				reader.close();
				reader = null;
			}
		}

		// read manifest
		try {
			FileManifest cached = getManifestCache(manifestId);
			if (cached != null)
				return cached;

			reader = new RevLogReader(manifestLogFile, manifestDatFile);
			RevLog revlog = reader.findDoc(manifestId);
			byte[] doc = reader.readDoc(revlog.getDocOffset(), revlog.getDocLength());
			// manifest id should be set here (id = revlog id)
			FileManifest manifest = FileManifest.deserialize(doc, noConfigs);

			// legacy format upgrade
			if (manifest.getVersion() == 1)
				FileManifest.upgradeManifest(manifest, dbDir);

			manifest.setId(manifestId);

			setManifestCache(manifest);
			return manifest;
		} catch (FileNotFoundException e) {
			return new FileManifest();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	public ManifestIterator getManifestIterator(TreeSet<Integer> logRev) throws IOException {
		RevLogReader manifestReader = null;
		RevLogReader changeLogReader = null;
		try {
			manifestReader = new RevLogReader(manifestLogFile, manifestDatFile);
			changeLogReader = new RevLogReader(changeLogFile, changeDatFile);
			FileManifestIterator manifestIterator = new FileManifestIterator(manifestReader, changeLogReader, dbDir, logRev);

			return manifestIterator;
		} catch (IOException e) {
			if (manifestReader != null)
				manifestReader.close();
			throw e;
		}
	}

	@Override
	public List<CommitLog> getCommitLogs() {
		return getCommitLogs(0, Long.MAX_VALUE);
	}

	@Override
	public List<CommitLog> getCommitLogs(long offset, long limit) {
		List<CommitLog> commitLogs = new ArrayList<CommitLog>();
		RevLogReader reader = null;
		try {
			reader = new RevLogReader(changeLogFile, changeDatFile);

			ListIterator<RevLog> it = reader.iterator(offset);
			for (long i = 0; i < limit; i++) {
				if (!it.hasNext())
					break;
				RevLog revlog = it.next();
				byte[] doc = reader.readDoc(revlog.getDocOffset(), revlog.getDocLength());
				ChangeLog change = ChangeLog.deserialize(doc);
				change.setRev(revlog.getDocId());
				commitLogs.add(change);
			}
			return commitLogs;
		} catch (FileNotFoundException e) {
			return commitLogs;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	@Override
	public long getCommitCount() {
		RevLogReader reader = null;
		try {
			reader = new RevLogReader(changeLogFile, changeDatFile);
			Long count = reader.count();
			return count;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	@Override
	public ConfigTransaction beginTransaction() {
		return beginTransaction(defaultTimeout);
	}

	@Override
	public ConfigTransaction beginTransaction(int timeout) {
		FileConfigTransaction xact = new FileConfigTransaction(this);
		xact.begin(timeout);
		return xact;
	}

	@Override
	public void rollback(int changeset) {
		rollback(changeset, null, "rollback to rev " + changeset);
	}

	@Override
	public void rollback(int changeset, String committer, String log) {
		Manifest manifest = getManifest(changeset);

		try {
			lock();
			List<ConfigChange> emptyChangeSet = new ArrayList<ConfigChange>();
			ChangeSetWriter.log(changeLogFile, changeDatFile, emptyChangeSet, manifest.getId(), committer, log);
		} catch (IOException e) {
			throw new RollbackException(e);
		} finally {
			unlock();
		}
	}

	/**
	 * Delete all related files from file system. You cannot restore any data
	 * after purge().
	 * 
	 * @throws IOException
	 */
	public void purge() throws IOException {
		try {
			clearAllCaches();
			lock();

			// TODO: retry until deleted (other process may hold it)

			// delete all collections
			for (File f : dbDir.listFiles()) {
				String n = f.getName();
				if (n.startsWith("col") && (n.endsWith(".log") || n.endsWith(".dat")))
					f.delete();
			}

			// remove manifest and changelog
			manifestDatFile.delete();
			manifestLogFile.delete();
			changeLogFile.delete();
			changeDatFile.delete();
			counterFile.delete();
		} finally {
			unlock();
		}

		lockFile.delete();
		if (dbDir.listFiles().length == 0)
			dbDir.delete();
	}

	@Override
	public int count(Class<?> cls) {
		return count(cls, null);
	}

	@Override
	public int count(Class<?> cls, Predicate pred) {
		ConfigCollection collection = getCollection(cls);
		if (collection == null)
			throw new IllegalStateException("Collection not found: class " + cls.getName());
		return collection.count(pred);
	}

	@Override
	public int count(ConfigTransaction xact, Class<?> cls) {
		ConfigCollection collection = getCollection(cls);
		if (collection == null)
			throw new IllegalStateException("Collection not found: class " + cls.getName());
		return collection.count(xact);
	}

	@Override
	public ConfigIterator findAll(Class<?> cls) {
		ConfigCollection collection = getCollection(cls);
		if (collection == null)
			return new EmptyIterator();

		return collection.findAll();
	}

	@Override
	public ConfigIterator find(Class<?> cls, Predicate pred) {
		ConfigCollection collection = getCollection(cls);
		if (collection == null) {
			logger.debug("kraken confdb: db [{}] col [{}] not found, returns empty iterator", dbName, cls.getName());
			return new EmptyIterator();
		}

		return collection.find(pred);
	}

	@Override
	public Config findOne(Class<?> cls, Predicate pred) {
		ConfigCollection collection = getCollection(cls);
		if (collection == null)
			return null;

		return collection.findOne(pred);
	}

	@Override
	public Config add(Object doc) {
		if (doc == null)
			throw new IllegalArgumentException("doc cannot be null");

		ConfigTransaction xact = beginTransaction();
		try {
			ConfigCollection collection = ensureCollection(xact, doc.getClass());
			Config c = collection.add(xact, PrimitiveConverter.serialize(doc, new CascadeUpdate(xact.getCache())));
			xact.commit(null, null);
			return c;
		} catch (Throwable t) {
			xact.rollback();
			throw new RollbackException(t);
		}
	}

	@Override
	public Config add(Object doc, String committer, String log) {
		if (doc == null)
			throw new IllegalArgumentException("doc cannot be null");

		ConfigTransaction xact = beginTransaction();
		try {
			ConfigCollection collection = ensureCollection(xact, doc.getClass());
			Config c = collection.add(xact, PrimitiveConverter.serialize(doc, new CascadeUpdate(xact.getCache())));
			xact.commit(committer, log);
			return c;
		} catch (Throwable t) {
			xact.rollback();
			throw new RollbackException(t);
		}
	}

	@Override
	public Config add(ConfigTransaction xact, Object doc) {
		if (doc == null)
			throw new IllegalArgumentException("doc cannot be null");

		ConfigCollection collection = ensureCollection(xact, doc.getClass());
		return collection.add(xact, PrimitiveConverter.serialize(doc, new CascadeUpdate(xact.getCache())));
	}

	@Override
	public Config update(Config c, Object doc) {
		if (!setUpdate(c, doc))
			return c;
		return c.getCollection().update(c);
	}

	@Override
	public Config update(Config c, Object doc, boolean checkConflict) {
		if (!setUpdate(c, doc))
			return c;
		return c.getCollection().update(c, checkConflict);
	}

	@Override
	public Config update(Config c, Object doc, boolean checkConflict, String committer, String log) {
		if (!setUpdate(c, doc))
			return c;
		return c.getCollection().update(c, checkConflict, committer, log);
	}

	@Override
	public Config update(ConfigTransaction xact, Config c, Object doc, boolean checkConflict) {
		if (!setUpdate(c, doc, xact))
			return c;
		return c.getCollection().update(xact, c, checkConflict);
	}

	private boolean setUpdate(Config c, Object doc) {
		return setUpdate(c, doc, null);
	}

	private boolean setUpdate(Config c, Object doc, ConfigTransaction xact) {
		if (doc == null)
			throw new IllegalArgumentException("doc cannot be null");

		ConfigTransactionCache cache = null;
		if (xact != null)
			xact.getCache();

		Object newDoc = PrimitiveConverter.serialize(doc, new CascadeUpdate(cache));
		if (newDoc.equals(c.getDocument()))
			return false;
		c.setDocument(newDoc);
		return true;
	}

	private class CascadeUpdate implements PrimitiveSerializeCallback {
		private ConfigTransactionCache cache;

		public CascadeUpdate(ConfigTransactionCache cache) {
			this.cache = cache;
		}

		@Override
		public void onSerialize(Object root, Class<?> cls, Object obj, Map<String, Object> referenceKeys) {
			ConfigCollection coll = ensureCollection(cls);
			Object oldDoc = cache.get(cls, new ReferenceKeys(referenceKeys));
			Config c = null;
			if (oldDoc == null) {
				c = coll.findOne(Predicates.field(referenceKeys));
				if (c != null) {
					oldDoc = c.getDocument();
					cache.put(cls, oldDoc);
				}
			}

			if (oldDoc == null)
				add(obj);
			else {
				Object serialized = PrimitiveConverter.serialize(obj, this);
				if (!serialized.equals(oldDoc)) {
					if (c == null)
						c = coll.findOne(Predicates.field(referenceKeys));

					// c must exists
					c.setDocument(serialized);
					c.getCollection().update(c);
					cache.remove(cls, oldDoc);
					cache.put(cls, serialized);
				}
			}
		}
	}

	@Override
	public Config remove(Config c) {
		if (c == null)
			throw new IllegalArgumentException("config cannot be null");
		return c.getCollection().remove(c);
	}

	@Override
	public Config remove(Config c, boolean checkConflict) {
		if (c == null)
			throw new IllegalArgumentException("config cannot be null");
		return c.getCollection().remove(c, checkConflict);
	}

	@Override
	public Config remove(Config c, boolean checkConflict, String committer, String log) {
		if (c == null)
			throw new IllegalArgumentException("config cannot be null");
		return c.getCollection().remove(c, checkConflict, committer, log);
	}

	@Override
	public Config remove(ConfigTransaction xact, Config c, boolean checkConflict) {
		if (c == null)
			throw new IllegalArgumentException("config cannot be null");
		return c.getCollection().remove(xact, c, checkConflict);
	}

	@Override
	public void shrink(int count) {
		try {
			new Shrinker(this).shrink(count);
			clearAllCaches();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void importData(InputStream is) {
		try {
			new Importer(this).importData(is);
			clearAllCaches();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} catch (ParseException e) {
			throw new IllegalStateException(e);
		}

		for (ConfigDatabaseListener listener : listeners) {
			try {
				listener.onImport(this);
			} catch (Throwable t) {
				logger.error("kraken confdb: import database callback should not throw any exception", t);
			}
		}
	}

	@Override
	public void exportData(OutputStream os) {
		try {
			long begin = new Date().getTime();
			new Exporter(this).exportData(os);
			long end = new Date().getTime();
			logger.trace("kraken confdb: [{}] export is completed, elapsed {}ms", getName(), (end - begin));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private FileManifest getManifestCache(int rev) {
		ConcurrentMap<Integer, FileManifest> manifestMap = manifestCache.get();
		if (manifestMap == null)
			return null;

		FileManifest fileManifest = manifestMap.get(rev);
		if (fileManifest == null)
			return null;

		return fileManifest;
	}

	private void setManifestCache(FileManifest manifest) {
		ConcurrentMap<Integer, FileManifest> manifestMap = manifestCache.get();
		if (manifestMap == null) {
			manifestMap = new ConcurrentHashMap<Integer, FileManifest>();
			manifestCache = new WeakReference<ConcurrentMap<Integer, FileManifest>>(manifestMap);
		}

		manifestMap.put(manifest.getId(), manifest);
	}

	private Integer getCachedManifestId(int rev) {
		ConcurrentMap<Integer, Integer> changeMap = changeCache.get();
		if (changeMap == null)
			return null;

		return changeMap.get(rev);
	}

	private void setChangeSetCache(int changeDocId, int manifestId) {
		ConcurrentMap<Integer, Integer> changeMap = changeCache.get();
		if (changeMap == null) {
			changeMap = new ConcurrentHashMap<Integer, Integer>();
			changeCache = new WeakReference<ConcurrentMap<Integer, Integer>>(changeMap);
		}

		changeMap.put(changeDocId, manifestId);
	}

	@Override
	public ConfigCache getCache() {
		return configCache;
	}

	@Override
	public String toString() {
		return dbName + ", changeset=" + (changeset == null ? "tip" : changeset);
	}

	public List<RevLog> getSnapshotCache(int colId, int manifestId) {
		ConcurrentMap<SnapshotKey, List<RevLog>> snapshotMap = snapshotCache.get();
		if (snapshotMap == null) {
			return null;
		}

		return snapshotMap.get(new SnapshotKey(colId, manifestId));
	}

	public void setSnapshotCache(int colId, int manifestId, List<RevLog> snapshot) {
		ConcurrentMap<SnapshotKey, List<RevLog>> snapshotMap = snapshotCache.get();
		if (snapshotMap == null) {
			snapshotMap = new ConcurrentHashMap<SnapshotKey, List<RevLog>>();
			snapshotCache = new WeakReference<ConcurrentMap<SnapshotKey, List<RevLog>>>(snapshotMap);
		}

		snapshotMap.put(new SnapshotKey(colId, manifestId), snapshot);
	}

	private void clearAllCaches() {
		manifestCache.clear();
		snapshotCache.clear();
		configCache = new FileConfigCache(this);
	}

	private static class SnapshotKey {
		private int colId;
		private int manifestId;

		public SnapshotKey(int colId, int manifestId) {
			this.colId = colId;
			this.manifestId = manifestId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + colId;
			result = prime * result + manifestId;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SnapshotKey other = (SnapshotKey) obj;
			if (colId != other.colId)
				return false;
			if (manifestId != other.manifestId)
				return false;
			return true;
		}
	}

	@Override
	public void addListener(ConfigDatabaseListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener should not be null");
		listeners.add(listener);
	}

	@Override
	public void removeListener(ConfigDatabaseListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener should not be null");
		listeners.add(listener);
	}
}
