package org.krakenapps.confdb.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.krakenapps.confdb.CollectionEntry;
import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.ConfigChange;
import org.krakenapps.confdb.ConfigEntry;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.Manifest;

public class FileConfigTransaction implements ConfigTransaction {

	private FileConfigDatabase db;

	/**
	 * wait other transaction timeout in milliseconds
	 */
	private int timeout;

	private Manifest manifest;

	private List<ConfigChange> changeSet;

	private File changeLogFile;
	private File changeDatFile;
	private File manifestLogFile;
	private File manifestDatFile;

	public FileConfigTransaction(FileConfigDatabase db, int timeout) {
		this.db = db;
		this.timeout = timeout;
		File dbDir = db.getDbDirectory();

		// TODO: apply changeset rev
		manifest = db.getManifest(null);
		changeSet = new ArrayList<ConfigChange>();

		changeLogFile = new File(dbDir, "changeset.log");
		changeDatFile = new File(dbDir, "changeset.dat");
		manifestLogFile = new File(dbDir, "manifest.log");
		manifestDatFile = new File(dbDir, "manifest.dat");
	}

	@Override
	public Manifest getManifest() {
		return manifest;
	}

	@Override
	public void begin() {
		db.lock();
	}

	/**
	 * log all transaction
	 */
	@Override
	public void log(CommitOp op, String colName, int docId, long rev) {
		CollectionEntry col = null;

		if (op == CommitOp.CreateCol) {
			col = new CollectionEntry(db.nextCollectionId(), colName);
		} else
			col = manifest.getCollectionEntry(colName);

		ConfigEntry entry = new ConfigEntry(col.getId(), docId, rev);

		if (op == CommitOp.CreateDoc || op == CommitOp.UpdateDoc)
			manifest.add(entry);
		else if (op == CommitOp.DeleteDoc)
			manifest.remove(entry);
		else if (op == CommitOp.CreateCol)
			manifest.add(col);
		else if (op == CommitOp.DropCol)
			manifest.remove(col);

		changeSet.add(new ConfigChange(op, col.getName(), col.getId(), docId));
	}

	@Override
	public void commit(String committer, String log) {
		try {
			Manifest manifest = writeManifestLog();
			writeChangeLog(manifest.getId(), committer, log);

			db.unlock();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

	}

	@Override
	public void rollback() {
		// rollback other transaction

		db.unlock();
	}

	private Manifest writeManifestLog() throws IOException {
		RevLog log = new RevLog();
		log.setRev(1);
		log.setOperation(CommitOp.CreateDoc);
		log.setDoc(manifest.serialize());

		RevLogWriter writer = null;
		try {
			writer = new RevLogWriter(manifestLogFile, manifestDatFile);
			manifest.setId(writer.write(log));
			return manifest;
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	private void writeChangeLog(int manifestId, String committer, String log) throws IOException {
		ChangeLog change = new ChangeLog();
		change.setManifestId(manifestId);
		change.setCommitter(committer);
		change.setMessage(log);
		change.setChangeSet(changeSet);

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

}
