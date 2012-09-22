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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.CollectionEntry;
import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigEntry;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.Manifest;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.RollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileConfigCollection implements ConfigCollection {
	private final Logger logger = LoggerFactory.getLogger(FileConfigCollection.class.getName());

	private FileConfigDatabase db;

	/**
	 * head revision if null, otherwise it point to specific revision
	 */
	private Integer changeset;

	/**
	 * TODO: manifest should be updated when you commit
	 */

	/**
	 * collection metadata
	 */
	private CollectionEntry col;

	private File logFile;

	private File datFile;

	public FileConfigCollection(FileConfigDatabase db, Integer changeset, CollectionEntry col) throws IOException {
		File dbDir = db.getDbDirectory();
		boolean created = dbDir.mkdirs();
		if (created)
			logger.info("kraken confdb: created database dir [{}]", dbDir.getAbsolutePath());

		this.db = db;
		this.col = col;
		this.changeset = changeset;

		logFile = new File(dbDir, "col" + col.getId() + ".log");
		datFile = new File(dbDir, "col" + col.getId() + ".dat");
	}

	public int getId() {
		return col.getId();
	}

	@Override
	public String getName() {
		return col.getName();
	}

	@Override
	public int count() {
		Manifest manifest = db.getManifest(changeset);
		return manifest.getConfigEntries(col.getName()).size();
	}

	@Override
	public int count(Predicate pred) {
		try {
			ConfigIterator it = getIterator(pred);
			return it.count();
		} catch (IOException e) {
			throw new IllegalStateException("cannot count " + col.getName() + " of database " + db.getName(), e);
		}

	}

	@Override
	public int count(ConfigTransaction xact) {
		Manifest manifest = xact.getManifest();
		return manifest.getConfigEntries(col.getName()).size();
	}

	@Override
	public ConfigIterator findAll() {
		return find(null);
	}

	@Override
	public Config findOne(Predicate pred) {
		ConfigIterator it = null;
		Config c = null;

		try {
			it = find(pred);
			if (it.hasNext())
				c = it.next();

			return c;
		} finally {
			if (it != null)
				it.close();
		}
	}

	@Override
	public ConfigIterator find(Predicate pred) {
		try {
			return getIterator(pred);
		} catch (FileNotFoundException e) {
			// collection has no data
			logger.debug("kraken confdb: returns empty iterator, db [{}] col [{}]", db.getName(), col.getName());
			return new EmptyIterator();
		} catch (IOException e) {
			throw new IllegalStateException("cannot open collection file", e);
		}
	}

	private ConfigIterator getIterator(Predicate pred) throws IOException {
		Manifest manifest = db.getManifest(changeset);
		RevLogReader reader = new RevLogReader(logFile, datFile);
		List<RevLog> snapshot = getSnapshot(manifest, reader);
		if (logger.isDebugEnabled())
			logger.debug("kraken confdb: db [{}], col [{}], snapshot size [{}]", new Object[] { db.getName(), col.getName(),
					snapshot.size() });

		return new FileConfigIterator(db, manifest, this, reader, snapshot, pred);
	}

	private List<RevLog> getSnapshot(Manifest manifest, RevLogReader reader) throws IOException {
		List<RevLog> snapshot = db.getSnapshotCache(col.getId(), manifest.getId());
		if (snapshot != null) {
			if (logger.isDebugEnabled())
				logger.debug("kraken confdb: return cached snapshot, db [{}], col [{}], manifest [{}], snapshot size [{}]",
						new Object[] { db.getName(), col.getName(), manifest.getId(), snapshot.size() });
			return snapshot;
		}

		snapshot = new ArrayList<RevLog>();
		if (manifest.getVersion() == 1) {
			long count = reader.count();
			for (long index = 0; index < count; index++) {
				RevLog log = reader.read(index);
				// assume that rev is unique
				if ((log.getOperation() == CommitOp.CreateDoc || log.getOperation() == CommitOp.UpdateDoc)
						&& manifest.containsDoc(col.getName(), log.getDocId(), log.getRev()))
					snapshot.add(log);
			}
		} else if (manifest.getVersion() == 2) {
			List<ConfigEntry> configs = manifest.getConfigEntries(col.getName());
			for (ConfigEntry c : configs) {
				RevLog log = reader.read(c.getIndex());
				snapshot.add(log);

				if (logger.isDebugEnabled())
					logger.debug("kraken confdb: db [{}], col [{}], manifest [{}], revlog [{}]",
							new Object[] { db.getName(), col.getName(), manifest.getId(), log });
			}

			db.setSnapshotCache(col.getId(), manifest.getId(), snapshot);
		}

		return snapshot;
	}

	@Override
	public Config add(Object doc) {
		return add(doc, null, null);
	}

	@Override
	public Config add(Object doc, String committer, String log) {
		ConfigTransaction xact = db.beginTransaction();
		try {
			Config c = add(xact, doc);
			xact.commit(committer, log);
			return c;
		} catch (Throwable e) {
			xact.rollback();
			throw new RollbackException(e);
		}
	}

	@Override
	public Config add(ConfigTransaction xact, Object doc) {
		try {
			RevLogWriter writer = getWriter(xact);

			ByteBuffer bb = encodeDocument(doc);
			RevLog revlog = newLog(0, 0, CommitOp.CreateDoc, bb.array());

			// write collection log
			int docId = writer.write(revlog);
			int index = writer.count() - 1;

			// write db changelog
			xact.log(CommitOp.CreateDoc, col.getName(), docId, revlog.getRev(), index);
			return new FileConfig(db, this, docId, revlog.getRev(), revlog.getPrevRev(), doc);
		} catch (IOException e) {
			throw new IllegalStateException("cannot add object", e);
		}
	}

	private ByteBuffer encodeDocument(Object doc) {
		int len = EncodingRule.lengthOf(doc);
		ByteBuffer bb = ByteBuffer.allocate(len);
		EncodingRule.encode(bb, doc);
		return bb;
	}

	@Override
	public Config update(Config c) {
		return update(c, false);
	}

	@Override
	public Config update(Config c, boolean checkConflict) {
		return update(c, checkConflict, null, null);
	}

	@Override
	public Config update(Config c, boolean checkConflict, String committer, String log) {
		ConfigTransaction xact = db.beginTransaction();
		try {
			Config updated = update(xact, c, checkConflict);
			xact.commit(committer, log);
			return updated;
		} catch (Throwable e) {
			xact.rollback();
			throw new RollbackException(e);
		}
	}

	@Override
	public Config update(ConfigTransaction xact, Config c, boolean checkConflict) {
		RevLogReader reader = null;
		try {
			Manifest manifest = db.getManifest(changeset);
			RevLogWriter writer = getWriter(xact);
			reader = new RevLogReader(logFile, datFile);
			List<RevLog> snapshot = getSnapshot(manifest, reader);

			// find any conflict (if common parent exists)
			long lastRev = c.getRevision();
			for (RevLog log : snapshot) {
				if (log.getDocId() == c.getId() && log.getPrevRev() == lastRev) {
					if (checkConflict)
						throw new IllegalStateException("conflict with " + log.getRev());

					lastRev = log.getRev();
				}
			}

			ByteBuffer bb = encodeDocument(c.getDocument());
			RevLog revlog = newLog(c.getId(), lastRev, CommitOp.UpdateDoc, bb.array());

			// write collection log
			int id = writer.write(revlog);
			int index = writer.count() - 1;
			xact.log(CommitOp.UpdateDoc, col.getName(), id, revlog.getRev(), index);

			if (logger.isDebugEnabled())
				logger.debug("kraken confdb: updated db [{}] col [{}] doc [{}]",
						new Object[] { db.getName(), col.getName(), c.getDocument() });

			return new FileConfig(db, this, id, revlog.getRev(), revlog.getPrevRev(), c.getDocument());
		} catch (IOException e) {
			throw new IllegalStateException("cannot update object", e);
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	@Override
	public Config remove(Config c) {
		return remove(c, false);
	}

	@Override
	public Config remove(Config c, boolean checkConflict) {
		return remove(c, checkConflict, null, null);
	}

	@Override
	public Config remove(Config c, boolean checkConflict, String committer, String log) {
		ConfigTransaction xact = db.beginTransaction();
		Config config = c;
		try {
			config = remove(xact, c, checkConflict);
			xact.commit(committer, log);
			return config;
		} catch (Throwable e) {
			xact.rollback();
			throw new RollbackException(e);
		}
	}

	@Override
	public Config remove(ConfigTransaction xact, Config c, boolean checkConflict) {
		try {
			RevLogWriter writer = getWriter(xact);

			// TODO: check conflict
			RevLog revlog = newLog(c.getId(), c.getRevision(), CommitOp.DeleteDoc, null);

			// write collection log
			int id = writer.write(revlog);
			int index = writer.count() - 1;
			xact.log(CommitOp.DeleteDoc, col.getName(), id, revlog.getRev(), index);
			return new FileConfig(db, this, id, revlog.getRev(), revlog.getPrevRev(), null);
		} catch (IOException e) {
			throw new IllegalStateException("cannot remove object", e);
		}
	}

	private RevLog newLog(int id, long prev, CommitOp op, byte[] doc) {
		RevLog log = new RevLog();
		log.setDocId(id);
		log.setRev(prev + 1);
		log.setPrevRev(prev);
		log.setOperation(op);
		log.setDoc(doc);
		return log;
	}

	private RevLogWriter getWriter(ConfigTransaction xact) throws IOException {
		FileConfigTransaction fxact = (FileConfigTransaction) xact;
		RevLogWriter writer = fxact.getWriters().get(logFile);

		if (writer == null) {
			writer = new RevLogWriter(logFile, datFile);
			fxact.getWriters().put(logFile, writer);
		}
		return writer;
	}

	@Override
	public String toString() {
		return "[" + col.getId() + "] " + col.getName();
	}
}
