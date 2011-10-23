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
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.CollectionEntry;
import org.krakenapps.confdb.CommitLog;
import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.Manifest;
import org.krakenapps.confdb.RollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileConfigDatabase implements ConfigDatabase {
	private final Logger logger = LoggerFactory.getLogger(FileConfigDatabase.class.getName());

	private File baseDir;
	private File dbDir;
	private String name;

	/**
	 * changeset revision
	 */
	private Integer changeset;

	/**
	 * default waiting transaction timeout in milliseconds
	 */
	private int defaultTimeout = 5000;

	private File changeLogFile;
	private File changeDatFile;
	private File manifestLogFile;
	private File manifestDatFile;

	private File lockFile;
	private FileLock lock;

	public FileConfigDatabase(File baseDir, String name) throws IOException {
		this(baseDir, name, null);
	}

	public FileConfigDatabase(File baseDir, String name, Integer rev) throws IOException {
		this.baseDir = baseDir;
		this.name = name;
		this.dbDir = new File(baseDir, name);
		this.changeset = rev;

		changeLogFile = new File(dbDir, "changeset.log");
		changeDatFile = new File(dbDir, "changeset.dat");
		manifestLogFile = new File(dbDir, "manifest.log");
		manifestDatFile = new File(dbDir, "manifest.dat");
		lockFile = new File(dbDir, "write.lock");
	}

	/**
	 * acquire write lock
	 */
	public void lock() {
		try {
			lockFile.getParentFile().mkdirs();
			RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
			FileChannel channel = raf.getChannel();
			lock = channel.tryLock();
		} catch (IOException e) {
			throw new IllegalStateException("cannot acquire write lock", e);
		}
	}

	/**
	 * release write lock
	 */
	public void unlock() {
		try {
			if (lock != null) {
				lock.release();
				lock.channel().close();
			}
		} catch (IOException e) {
			throw new IllegalStateException("cannot release write lock", e);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	public int nextCollectionId() {
		// TODO: return max collection id + 1
		return new Random().nextInt(100);
	}

	@Override
	public ConfigCollection ensureCollection(String name) {
		try {
			Manifest manifest = getManifest(changeset);
			CollectionEntry col = manifest.getCollectionEntry(name);

			// create new collection if not exists
			if (col == null)
				col = createCollection(name);

			return new FileConfigCollection(this, changeset, col);
		} catch (IOException e) {
			logger.error("kraken confdb: cannot open collection file", e);
			return null;
		}
	}

	public Set<String> getCollectionNames() {
		Manifest manifest = getManifest(changeset);
		return manifest.getCollectionNames();
	}

	private CollectionEntry createCollection(String name) throws IOException {
		ConfigTransaction xact = beginTransaction();
		try {
			xact.log(CommitOp.CreateCol, name, 0, 0);
			xact.commit(null, null);
			int newColId = xact.getManifest().getCollectionId(name);
			return new CollectionEntry(newColId, name);
		} catch (Exception e) {
			xact.rollback();
			throw new RollbackException(e);
		}
	}

	@Override
	public void dropCollection(String name) {
		try {
			lock();
		} finally {
			unlock();
		}
	}

	public File getDbDirectory() {
		return new File(baseDir, name);
	}

	@Override
	public Manifest getManifest(Integer rev) {
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

			byte[] doc = reader.readDoc(revlog.getDocOffset(), revlog.getDocLength());
			ChangeLog change = ChangeLog.deserialize(doc);
			manifestId = change.getManifestId();
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
			reader = new RevLogReader(manifestLogFile, manifestDatFile);
			RevLog revlog = reader.findDoc(manifestId);
			byte[] doc = reader.readDoc(revlog.getDocOffset(), revlog.getDocLength());
			Map<String, Object> m = EncodingRule.decodeMap(ByteBuffer.wrap(doc));
			return PrimitiveConverter.parse(FileManifest.class, m);
		} catch (FileNotFoundException e) {
			return new FileManifest();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	@Override
	public List<CommitLog> getCommitLogs() {
		List<CommitLog> commitLogs = new ArrayList<CommitLog>();
		RevLogReader reader = null;
		try {
			reader = new RevLogReader(changeLogFile, changeDatFile);
			long count = reader.count();

			for (int i = 0; i < count; i++) {
				RevLog revlog = reader.read(i);
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
	public ConfigTransaction beginTransaction() {
		return beginTransaction(defaultTimeout);
	}

	@Override
	public ConfigTransaction beginTransaction(int timeout) {
		FileConfigTransaction xact = new FileConfigTransaction(this, timeout);
		xact.begin();
		return xact;
	}

	/**
	 * Delete all related files from file system. You cannot restore any data
	 * after purge().
	 * 
	 * @throws IOException
	 */
	public void purge() throws IOException {
		try {
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
		} finally {
			unlock();
		}
	}
}
