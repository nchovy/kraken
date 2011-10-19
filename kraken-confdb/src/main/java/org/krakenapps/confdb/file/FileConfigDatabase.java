package org.krakenapps.confdb.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.CommitLog;
import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.ConfigChange;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
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

	private File changeLogFile;
	private File changeDatFile;
	private File manifestLogFile;
	private File manifestDatFile;

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
			Manifest manifest = getManifest(changeset);
			CollectionEntry col = manifest.getCollectionEntry(name);

			// create new collection if not exists
			if (col == null)
				col = createCollection(name);

			return new FileConfigCollection(this, col);
		} catch (IOException e) {
			logger.error("kraken confdb: cannot open collection file", e);
			return null;
		}
	}

	public Set<String> getCollectionNames() {
		try {
			Manifest manifest = getManifest(changeset);
			return manifest.getCollectionNames();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private CollectionEntry createCollection(String name) throws IOException {
		try {
			lock();

			// reload (manifest can be updated)
			Manifest manifest = getManifest(changeset);
			int newColId = manifest.nextCollectionId();

			commit(CommitOp.CreateCol, new CollectionEntry(newColId, name), 0, 0, null, null);
			return new CollectionEntry(1, name);
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

	private Manifest getManifest(Integer rev) throws IOException {
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
			return new Manifest();
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
			return PrimitiveConverter.parse(Manifest.class, m);
		} catch (FileNotFoundException e) {
			return new Manifest();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	public void commit(CommitOp op, CollectionEntry col, int docId, long rev, String committer, String log)
			throws IOException {
		int manifestId = writeManifestLog(op, col, docId, rev);
		writeChangeLog(manifestId, op, col, docId, committer, log);
	}

	private int writeManifestLog(CommitOp op, CollectionEntry col, int docId, long rev) throws IOException {
		Manifest manifest = getManifest(changeset);
		ConfigEntry entry = new ConfigEntry(col.getId(), docId, rev);

		if (op == CommitOp.CreateDoc || op == CommitOp.UpdateDoc)
			manifest.add(entry);
		else if (op == CommitOp.DeleteDoc)
			manifest.remove(entry);
		else if (op == CommitOp.CreateCol)
			manifest.add(col);
		else if (op == CommitOp.DropCol)
			manifest.remove(col);

		RevLog log = new RevLog();
		log.setRev(1);
		log.setOperation(CommitOp.CreateDoc);
		log.setDoc(manifest.serialize());

		RevLogWriter writer = null;
		try {
			writer = new RevLogWriter(manifestLogFile, manifestDatFile);
			return writer.write(log);
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	private void writeChangeLog(int manifestId, CommitOp op, CollectionEntry col, int docId, String committer, String log)
			throws IOException {
		ChangeLog change = new ChangeLog();
		change.setManifestId(manifestId);
		change.setCommitter(committer);
		change.setMessage(log);
		change.getChangeSet().add(new ConfigChange(op, col.getName(), col.getId(), docId));

		RevLog cl = new RevLog();
		cl.setRev(1);
		cl.setOperation(CommitOp.CreateDoc);
		cl.setDoc(change.serialize());

		RevLogWriter writer = null;
		try {
			writer = new RevLogWriter(changeLogFile, changeDatFile);
			writer.write(cl);
		} finally {
			if (writer != null)
				writer.close();
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
