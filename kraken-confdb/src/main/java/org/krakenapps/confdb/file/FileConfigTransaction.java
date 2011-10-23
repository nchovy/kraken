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
			ChangeSetWriter.log(changeLogFile, changeDatFile, changeSet, manifest.getId(), committer, log);

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

}
