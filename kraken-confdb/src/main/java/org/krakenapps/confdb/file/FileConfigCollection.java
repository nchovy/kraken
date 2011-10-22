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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.CollectionEntry;
import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigListener;
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
	 * TODO: manifest should be updated when you commit
	 */
	private Manifest manifest;

	/**
	 * collection metadata
	 */
	private CollectionEntry col;

	private File logFile;

	private File datFile;

	public FileConfigCollection(FileConfigDatabase db, Manifest manifest, CollectionEntry col) throws IOException {
		File dbDir = db.getDbDirectory();
		boolean created = dbDir.mkdirs();
		if (created)
			logger.info("kraken confdb: created database dir [{}]", dbDir.getAbsolutePath());

		this.db = db;
		this.col = col;
		this.manifest = manifest;

		logFile = new File(dbDir, "col" + col.getId() + ".log");
		datFile = new File(dbDir, "col" + col.getId() + ".dat");
	}

	@Override
	public String getName() {
		return col.getName();
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
			it.close();
		}
	}

	@Override
	public ConfigIterator find(Predicate pred) {
		try {
			return getIterator(pred);
		} catch (IOException e) {
			throw new IllegalStateException("cannot open collection file", e);
		}
	}

	private ConfigIterator getIterator(Predicate pred) throws IOException {
		RevLogReader reader = new RevLogReader(logFile, datFile);
		List<RevLog> snapshot = getSnapshot(reader);
		return new FileConfigIterator(db, this, reader, snapshot, pred);
	}

	private List<RevLog> getSnapshot(RevLogReader reader) throws IOException {
		List<RevLog> snapshot = new ArrayList<RevLog>();

		for (long index = 0; index < reader.count(); index++) {
			RevLog log = reader.read(index);
			if (manifest.containsDoc(col.getName(), log.getDocId(), log.getRev()))
				snapshot.add(log);
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
		} catch (Exception e) {
			xact.rollback();
			throw new RollbackException(e);
		}
	}

	@Override
	public Config add(ConfigTransaction xact, Object doc) {
		RevLogWriter writer = null;
		try {
			writer = new RevLogWriter(logFile, datFile);

			// TODO: check previous revision, is need update?

			ByteBuffer bb = encodeDocument(doc);
			RevLog revlog = newLog(0, 0, CommitOp.CreateDoc, bb.array());

			// write collection log
			int docId = writer.write(revlog);

			// write db changelog
			xact.log(CommitOp.CreateDoc, col.getName(), docId, revlog.getRev());
			manifest = xact.getManifest();
			return new FileConfig(db, this, docId, revlog.getRev(), revlog.getPrevRev(), doc);
		} catch (IOException e) {
			throw new IllegalStateException("cannot add object", e);
		} finally {
			if (writer != null)
				writer.close();
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
		return update(c, true);
	}

	@Override
	public Config update(Config c, boolean ignoreConflict) {
		return update(c, ignoreConflict, null, null);
	}

	@Override
	public Config update(Config c, boolean ignoreConflict, String committer, String log) {
		ConfigTransaction xact = db.beginTransaction();
		try {
			Config updated = update(xact, c, ignoreConflict);
			xact.commit(committer, log);
			return updated;
		} catch (Exception e) {
			xact.rollback();
			throw new RollbackException(e);
		}
	}

	@Override
	public Config update(ConfigTransaction xact, Config c, boolean ignoreConflict) {
		RevLogWriter writer = null;
		ConfigIterator it = null;
		try {
			writer = new RevLogWriter(logFile, datFile);
			it = getIterator(null);

			// find any conflict (if common parent exists)
			if (!ignoreConflict) {
				while (it.hasNext()) {
					Config o = it.next();
					if (o.getId() == c.getId() && o.getPrevRevision() == c.getRevision())
						throw new IllegalStateException("conflict with " + o.getRevision());
				}
			}

			ByteBuffer bb = encodeDocument(c.getDocument());
			RevLog revlog = newLog(c.getId(), c.getRevision(), CommitOp.UpdateDoc, bb.array());

			// write collection log
			int id = writer.write(revlog);
			xact.log(CommitOp.UpdateDoc, col.getName(), id, revlog.getRev());
			manifest = xact.getManifest();
			return new FileConfig(db, this, id, revlog.getRev(), revlog.getPrevRev(), c.getDocument());
		} catch (IOException e) {
			throw new IllegalStateException("cannot update object", e);
		} finally {
			if (writer != null)
				writer.close();

			if (it != null)
				it.close();
		}
	}

	@Override
	public Config remove(Config c) {
		return remove(c, true);
	}

	@Override
	public Config remove(Config c, boolean ignoreConflict) {
		return remove(c, ignoreConflict, null, null);
	}

	@Override
	public Config remove(Config c, boolean ignoreConflict, String committer, String log) {
		ConfigTransaction xact = db.beginTransaction();
		Config config = c;
		try {
			config = remove(xact, c, ignoreConflict);
			xact.commit(committer, log);
			return config;
		} catch (Exception e) {
			xact.rollback();
			throw new RollbackException(e);
		}
	}

	@Override
	public Config remove(ConfigTransaction xact, Config c, boolean ignoreConflict) {
		RevLogWriter writer = null;
		try {
			writer = new RevLogWriter(logFile, datFile);

			// TODO: check conflict
			RevLog revlog = newLog(c.getId(), c.getRevision(), CommitOp.DeleteDoc, null);

			// write collection log
			int id = writer.write(revlog);
			xact.log(CommitOp.DeleteDoc, col.getName(), id, revlog.getRev());
			manifest = xact.getManifest();
			return new FileConfig(db, this, id, revlog.getRev(), revlog.getPrevRev(), null);
		} catch (IOException e) {
			throw new IllegalStateException("cannot remove object", e);
		} finally {
			if (writer != null)
				writer.close();
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

	@Override
	public void addHook(CommitOp op, ConfigListener listener) {
	}

	@Override
	public void removeHook(CommitOp op, ConfigListener listener) {
	}
}
