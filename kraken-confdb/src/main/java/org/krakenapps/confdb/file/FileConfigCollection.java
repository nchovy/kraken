package org.krakenapps.confdb.file;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
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
	private CollectionMetadata meta;

	private File logFile;

	private File datFile;

	public FileConfigCollection(FileConfigDatabase db, CollectionMetadata meta) throws IOException {
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
	public ConfigIterator find(Predicate pred) {
		try {
			return getIterator(pred);
		} catch (IOException e) {
			throw new IllegalStateException("cannot open collection file", e);
		}
	}

	private ConfigIterator getIterator(Predicate pred) throws IOException {
		CollectionLogReader reader = new CollectionLogReader(db, this, logFile, datFile);
		List<CollectionLog> snapshot = getSnapshot(reader);
		return new FileConfigIterator(reader, snapshot, pred);
	}

	private List<CollectionLog> getSnapshot(CollectionLogReader reader) throws IOException {
		Map<Integer, CollectionLog> m = new TreeMap<Integer, CollectionLog>();

		for (long index = 0; index < reader.count(); index++) {
			CollectionLog log = reader.read(index);
			System.out.println(log);

			if (log.getOperation() == CommitOp.DeleteDoc)
				m.remove(log.getDocId());
			else if (log.getOperation() == CommitOp.CreateDoc || log.getOperation() == CommitOp.UpdateDoc)
				m.put(log.getDocId(), log);
		}

		return new ArrayList<CollectionLog>(m.values());
	}

	@Override
	public Config findOne(Predicate pred) {

		return null;
	}

	@Override
	public Config add(Object doc) {
		CollectionLogWriter writer = null;
		try {
			writer = new CollectionLogWriter(logFile, datFile);

			// TODO: check previous revision, is need update?

			ByteBuffer bb = encodeDocument(doc);
			CollectionLog log = newLog(0, 0, CommitOp.CreateDoc, bb.array());

			// write collection log
			int id = writer.write(log);
			return new FileConfig(db, this, id, log.getRev(), log.getPrevRev(), doc);
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
		CollectionLogWriter writer = null;
		try {
			writer = new CollectionLogWriter(logFile, datFile);

			Iterator<Config> it = getIterator(null);

			// find any conflict (if common parent exists)
			if (!ignoreConflict) {
				while (it.hasNext()) {
					Config o = it.next();
					if (o.getId() == c.getId() && o.getPrevRevision() == c.getRevision())
						throw new IllegalStateException("conflict with " + o.getRevision());
				}
			}

			ByteBuffer bb = encodeDocument(c.getDocument());
			CollectionLog log = newLog(c.getId(), c.getRevision(), CommitOp.UpdateDoc, bb.array());

			// write collection log
			int id = writer.write(log);
			return new FileConfig(db, this, id, log.getRev(), log.getPrevRev(), c.getDocument());
		} catch (IOException e) {
			throw new IllegalStateException("cannot update object", e);
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	@Override
	public Config remove(Config c) {
		return remove(c, true);
	}

	@Override
	public Config remove(Config c, boolean ignoreConflict) {
		CollectionLogWriter writer = null;
		try {
			writer = new CollectionLogWriter(logFile, datFile);

			// TODO: check conflict
			CollectionLog log = newLog(c.getId(), c.getRevision(), CommitOp.DeleteDoc, null);

			// write collection log
			int id = writer.write(log);
			return new FileConfig(db, this, id, log.getRev(), log.getPrevRev(), null);
		} catch (IOException e) {
			throw new IllegalStateException("cannot remove object", e);
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	private CollectionLog newLog(int id, long prev, CommitOp op, byte[] doc) {
		CollectionLog log = new CollectionLog();
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
