package org.krakenapps.confdb.file;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigListener;
import org.krakenapps.confdb.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileConfigCollection implements ConfigCollection {
	private final Logger logger = LoggerFactory.getLogger(FileConfigCollection.class.getName());

	private FileConfigDatabase db;

	/**
	 * collection metadata
	 */
	private CollectionEntry meta;

	private File logFile;

	private File datFile;

	public FileConfigCollection(FileConfigDatabase db, CollectionEntry meta) throws IOException {
		File dbDir = db.getDbDirectory();
		boolean created = dbDir.mkdirs();
		if (created)
			logger.info("kraken confdb: created database dir [{}]", dbDir.getAbsolutePath());

		this.db = db;
		this.meta = meta;

		logFile = new File(dbDir, "col" + meta.getId() + ".log");
		datFile = new File(dbDir, "col" + meta.getId() + ".dat");
	}

	@Override
	public String getName() {
		return meta.getName();
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
		Map<Integer, RevLog> m = new TreeMap<Integer, RevLog>();

		for (long index = 0; index < reader.count(); index++) {
			RevLog log = reader.read(index);

			if (log.getOperation() == CommitOp.DeleteDoc)
				m.remove(log.getDocId());
			else if (log.getOperation() == CommitOp.CreateDoc || log.getOperation() == CommitOp.UpdateDoc)
				m.put(log.getDocId(), log);
		}

		return new ArrayList<RevLog>(m.values());
	}

	@Override
	public Config add(Object doc) {
		return add(doc, null, null);
	}

	@Override
	public Config add(Object doc, String committer, String log) {
		RevLogWriter writer = null;
		try {
			db.lock();
			writer = new RevLogWriter(logFile, datFile);

			// TODO: check previous revision, is need update?

			ByteBuffer bb = encodeDocument(doc);
			RevLog revlog = newLog(0, 0, CommitOp.CreateDoc, bb.array());

			// write collection log
			int docId = writer.write(revlog);

			// write db changelog
			db.commit(CommitOp.CreateDoc, meta, docId, revlog.getRev(), committer, log);
			return new FileConfig(db, this, docId, revlog.getRev(), revlog.getPrevRev(), doc);
		} catch (IOException e) {
			throw new IllegalStateException("cannot add object", e);
		} finally {
			if (writer != null)
				writer.close();

			db.unlock();
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
		RevLogWriter writer = null;
		ConfigIterator it = null;
		try {
			db.lock();

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
			RevLog revlog = newLog(c.getId(), c.getRevision() + 1, CommitOp.UpdateDoc, bb.array());

			// write collection log
			int id = writer.write(revlog);
			db.commit(CommitOp.UpdateDoc, meta, id, revlog.getRev(), committer, log);
			return new FileConfig(db, this, id, revlog.getRev(), revlog.getPrevRev(), c.getDocument());
		} catch (IOException e) {
			throw new IllegalStateException("cannot update object", e);
		} finally {
			if (writer != null)
				writer.close();

			if (it != null)
				it.close();

			db.unlock();
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
		RevLogWriter writer = null;
		try {
			db.lock();
			writer = new RevLogWriter(logFile, datFile);

			// TODO: check conflict
			RevLog revlog = newLog(c.getId(), c.getRevision(), CommitOp.DeleteDoc, null);

			// write collection log
			int id = writer.write(revlog);
			db.commit(CommitOp.DeleteDoc, meta, id, revlog.getRev(), committer, log);
			return new FileConfig(db, this, id, revlog.getRev(), revlog.getPrevRev(), null);
		} catch (IOException e) {
			throw new IllegalStateException("cannot remove object", e);
		} finally {
			if (writer != null)
				writer.close();

			db.unlock();
		}
	}

	private RevLog newLog(int id, long prev, CommitOp op, byte[] doc) {
		RevLog log = new RevLog();
		log.setDocId(id);
		log.setRev(db.getNextRevision());
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
