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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.CollectionEntry;
import org.krakenapps.confdb.CommitLog;
import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.ConfigChange;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.Manifest;
import org.krakenapps.confdb.RollbackException;
import org.krakenapps.confdb.WriteLockTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileConfigDatabase implements ConfigDatabase {
	private final Logger logger = LoggerFactory.getLogger(FileConfigDatabase.class.getName());

	private File baseDir;
	private File dbDir;
	private String name;

	/**
	 * base changeset revision
	 */
	private Integer changeset;

	/**
	 * default waiting transaction timeout in milliseconds
	 */
	private int defaultTimeout = 5000;

	private final File changeLogFile;
	private final File changeDatFile;
	private final File manifestLogFile;
	private final File manifestDatFile;
	private final File counterFile;
	private final File lockFile;

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
		counterFile = new File(dbDir, "col.id");
	}

	/**
	 * acquire write lock
	 */
	public void lock() {
		lock(defaultTimeout);
	}

	public void lock(int timeout) {
		Date begin = new Date();
		RandomAccessFile raf = null;
		FileChannel channel = null;
		try {
			lockFile.getParentFile().mkdirs();
			raf = new RandomAccessFile(lockFile, "rw");
			channel = raf.getChannel();

			while (lock == null) {
				// check lock timeout
				Date now = new Date();
				if (now.getTime() - begin.getTime() > timeout)
					throw new WriteLockTimeoutException();

				lock = channel.tryLock();
				Thread.sleep(100);
			}
		} catch (IOException e) {
			throw new IllegalStateException("cannot acquire write lock", e);
		} catch (InterruptedException e) {
			throw new IllegalStateException("cannot acquire write lock", e);
		} finally {
			// close channel if lock failed
			if (lock == null && channel != null) {
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
		try {
			if (lock != null) {
				lock.release();
				lock.channel().close();
				lock = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("cannot release write lock", e);
		}
	}

	@Override
	public String getName() {
		return name;
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

			// manifest id should be set here (id = revlog id)
			FileManifest manifest = PrimitiveConverter.parse(FileManifest.class, m);
			manifest.setId(manifestId);
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
		FileConfigTransaction xact = new FileConfigTransaction(this);
		xact.begin(timeout);
		return xact;
	}

	@Override
	public void rollback(int changeset) {
		rollback(changeset, null, null);
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
	}
}
