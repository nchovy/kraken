package org.krakenapps.confdb.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.Predicate;

/**
 * Build collection snapshot based on collection log. Snapshot contains only doc
 * pointers, and read actual doc data when you call next()
 * 
 * @author xeraph
 * 
 */
class FileConfigIterator implements ConfigIterator {
	private ConfigDatabase db;
	private ConfigCollection col;
	private RevLogReader reader;
	private Iterator<RevLog> it;
	private Predicate pred;

	public FileConfigIterator(ConfigDatabase db, ConfigCollection col, RevLogReader reader, List<RevLog> snapshot,
			Predicate pred) {
		this.db = db;
		this.col = col;
		this.reader = reader;
		this.it = snapshot.iterator();
		this.pred = pred;
	}

	@Override
	public boolean hasNext() {
		if (!it.hasNext())
			reader.close();

		return it.hasNext();
	}

	@Override
	public Config next() {
		try {
			if (!it.hasNext())
				throw new NoSuchElementException("no more config item in collection");

			RevLog log = it.next();

			// fetch doc binary and decode it
			byte[] b = reader.readDoc(log.getDocOffset(), log.getDocLength());
			Object doc = EncodingRule.decode(ByteBuffer.wrap(b));

			return new FileConfig(db, col, log.getDocId(), log.getRev(), log.getPrevRev(), doc);
		} catch (IOException e) {
			throw new IllegalStateException("cannot read next item", e);
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		reader.close();
	}
}
