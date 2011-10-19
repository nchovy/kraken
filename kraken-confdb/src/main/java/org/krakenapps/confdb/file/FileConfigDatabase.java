package org.krakenapps.confdb.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.CommitLog;
import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileConfigDatabase implements ConfigDatabase {
	private final Logger logger = LoggerFactory.getLogger(FileConfigDatabase.class.getName());

	private File baseDir;
	private File dbDir;
	private String name;

	private AtomicLong revCounter;

	private File changeLogFile;
	private File changeDatFile;
	private File manifestLogFile;
	private File manifestDatFile;

	private Map<Integer, CollectionMetadata> colMetadatas;
	private ConcurrentMap<Integer, FileConfigCollection> collections;

	public FileConfigDatabase(File baseDir, String name) throws IOException {
		this.baseDir = baseDir;
		this.name = name;
		this.dbDir = new File(baseDir, name);

		// TODO: load last revision
		this.revCounter = new AtomicLong();

		this.colMetadatas = new HashMap<Integer, CollectionMetadata>();
		this.collections = new ConcurrentHashMap<Integer, FileConfigCollection>();

		changeLogFile = new File(dbDir, "changeset.log");
		changeDatFile = new File(dbDir, "changeset.dat");
		manifestLogFile = new File(dbDir, "manifest.log");
		manifestDatFile = new File(dbDir, "manifest.dat");
	}

	/**
	 * acquire write lock
	 */
	public void lock() {

	}

	/**
	 * release write lock
	 */
	public void unlock() {

	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ConfigCollection ensureCollection(String name) {
		try {
			CollectionMetadata meta = getCollectionMetadata(name);

			// create new collection if not exists
			if (meta == null)
				meta = createCollection(name);

			// is collection already loaded?
			FileConfigCollection col = new FileConfigCollection(this, meta);

			// TODO: collection loading management
			return col;
		} catch (IOException e) {
			logger.error("kraken confdb: cannot open collection file", e);
			return null;
		}
	}

	private CollectionMetadata createCollection(String name) {
		try {
			lock();
			return new CollectionMetadata(1, name);
		} finally {
			unlock();
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

	public CollectionMetadata getCollectionMetadata(String name) {
		for (int id : colMetadatas.keySet()) {
			CollectionMetadata meta = colMetadatas.get(id);
			if (meta.getName().equals(name))
				return meta;
		}

		return null;
	}

	public String getCollectionName(int id) {
		if (colMetadatas.containsKey(id))
			return colMetadatas.get(id).getName();

		return null;
	}

	public long getNextRevision() {
		return revCounter.incrementAndGet();
	}

	public void commit(CommitOp op, CollectionMetadata col, int docId, String committer, String log) throws IOException {
		ChangeLog change = new ChangeLog();
		change.setCommitter(committer);
		change.setMessage(log);

		RevLog cl = new RevLog();
		cl.setRev(getNextRevision());
		cl.setOperation(CommitOp.CreateDoc);
		cl.setDoc(change.serialize());

		RevLogWriter changeWriter = null;
		try {
			changeWriter = new RevLogWriter(changeLogFile, changeDatFile);
			changeWriter.write(cl);
		} finally {
			if (changeWriter != null)
				changeWriter.close();
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
				System.out.println(revlog);
				ChangeLog change = ChangeLog.deserialize(doc);
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

	/**
	 * Delete all related files from file system. You cannot restore any data
	 * after purge().
	 */
	public void purge() {
		// TODO: close all file handles

		// TODO: collection file removal

		// TODO: retry until deleted (other process may hold it)
		manifestDatFile.delete();
		manifestLogFile.delete();
		changeLogFile.delete();
		changeDatFile.delete();
	}
}
