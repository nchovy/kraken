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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.krakenapps.api.PrimitiveParseCallback;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCache;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigParser;
import org.krakenapps.confdb.Manifest;
import org.krakenapps.confdb.ObjectBuilder;
import org.krakenapps.confdb.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build collection snapshot based on collection log. Snapshot contains only doc
 * pointers, and read actual doc data when you call next()
 * 
 * @author xeraph
 * 
 */
class FileConfigIterator implements ConfigIterator {
	private final Logger logger = LoggerFactory.getLogger(FileConfigIterator.class.getName());
	private ConfigDatabase db;
	private ConfigCache cache;
	private int manifestId;
	private ConfigCollection col;
	private String colName;
	private RevLogReader reader;
	private Iterator<RevLog> it;
	private Predicate pred;
	private Config prefetch;
	private boolean loaded;
	private boolean closed;
	private ConfigParser parser;

	public FileConfigIterator(ConfigDatabase db, Manifest manifest, ConfigCollection col, RevLogReader reader,
			List<RevLog> snapshot, Predicate pred) {
		this.db = db;
		this.manifestId = manifest.getId();
		this.cache = db.getCache();
		this.col = col;
		this.colName = col.getName();
		this.reader = reader;
		this.it = snapshot.iterator();
		this.pred = pred;

		if (logger.isDebugEnabled())
			logger.debug("kraken confdb: db [{}], col [{}], snapshot size [{}], iterator hash code [{}]",
					new Object[] { db.getName(), col.getName(), snapshot.size(), hashCode() });
	}

	@Override
	public boolean hasNext() {
		if (closed)
			return false;

		if (loaded)
			return prefetch != null;

		try {
			prefetch = getNextMatch();
		} catch (IOException e) {
			close();
			return false;
		}

		return prefetch != null;
	}

	@Override
	public Config next() {
		if (!it.hasNext() && !loaded)
			throw new NoSuchElementException("no more config item in collection");

		if (it.hasNext() && !loaded) {
			if (!hasNext()) {
				close();
				throw new NoSuchElementException("no more config item in collection");
			}
		}

		loaded = false;
		Config r = prefetch;
		prefetch = null;
		return r;
	}

	private Config getNextMatch() throws IOException {
		if (loaded)
			return prefetch;

		Config matched = null;
		while (it.hasNext()) {
			Config c = getNextConfig();
			if (logger.isDebugEnabled())
				logger.debug("kraken confdb: db [{}], col [{}], config [{}]",
						new Object[] { db.getName(), col.getName(), c.getDocument() });

			if (pred == null || pred.eval(c)) {
				matched = c.duplicate();
				break;
			}
		}

		loaded = (matched != null);

		if (loaded == false)
			close();

		return matched;
	}

	private Config getNextConfig() throws IOException {
		RevLog log = it.next();

		// check cache
		Config cachedConfig = cache.findEntry(colName, manifestId, log.getDocId(), log.getRev());
		if (cachedConfig != null) {
			Object doc = cachedConfig.getDocument();
			return new FileConfig(db, col, log.getDocId(), log.getRev(), log.getPrevRev(), doc, parser);
		}

		// fetch doc binary and decode it
		byte[] b = reader.readDoc(log.getDocOffset(), log.getDocLength());
		Object doc = EncodingRule.decode(ByteBuffer.wrap(b));
		FileConfig config = new FileConfig(db, col, log.getDocId(), log.getRev(), log.getPrevRev(), doc, parser);
		cache.putEntry(colName, manifestId, config);
		return config;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setParser(ConfigParser parser) {
		this.parser = parser;
	}

	@Override
	public List<Config> getConfigs(int offset, int limit) {
		try {
			int p = 0;
			int count = 0;

			ArrayList<Config> configs = new ArrayList<Config>();
			while (hasNext()) {
				if (count >= limit)
					break;

				Config next = next();
				if (p++ < offset)
					continue;

				configs.add(next);
				count++;
			}
			return configs;
		} finally {
			close();
		}
	}

	@Override
	public Collection<Object> getDocuments() {
		try {
			Collection<Object> docs = new ArrayList<Object>();
			while (hasNext())
				docs.add(next().getDocument());
			return docs;
		} finally {
			close();
		}
	}

	@Override
	public <T> Collection<T> getDocuments(Class<T> cls) {
		return getDocuments(cls, null);
	}

	@Override
	public <T> Collection<T> getDocuments(Class<T> cls, PrimitiveParseCallback callback) {
		return getDocuments(cls, callback, 0, Integer.MAX_VALUE);
	}

	@Override
	public <T> Collection<T> getDocuments(Class<T> cls, PrimitiveParseCallback callback, int offset, int limit) {
		try {
			int p = 0;
			int count = 0;

			Collection<T> docs = new ArrayList<T>();
			while (hasNext()) {
				if (count >= limit)
					break;

				Config next = next();
				if (p++ < offset)
					continue;

				docs.add(next.getDocument(cls, callback));
				count++;
			}
			return docs;
		} finally {
			close();
		}
	}

	@Override
	public <T> Collection<T> getObjects(ObjectBuilder<T> builder, int offset, int limit) {
		try {
			int p = 0;
			int count = 0;

			Collection<T> objects = new ArrayList<T>();
			while (hasNext()) {
				if (count >= limit)
					break;

				Config next = next();
				if (p++ < offset)
					continue;

				T obj = builder.build(next);
				if (obj == null)
					continue;
				objects.add(obj);
				count++;
			}
			return objects;
		} finally {
			close();
		}
	}

	@Override
	public int count() {
		int total = 0;

		try {
			while (it.hasNext()) {
				Config c = getNextConfig();
				if (pred == null || pred.eval(c))
					total++;
			}
		} catch (IOException e) {
			throw new IllegalStateException("cannot count collection " + col.getName() + " of database " + db.getName(), e);
		} finally {
			close();
		}
		return total;
	}

	@Override
	public void close() {
		if (!closed) {
			reader.close();
			closed = true;
		}
	}
}
