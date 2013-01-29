/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logstorage.engine;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.krakenapps.codec.EncodingRule;
import org.krakenapps.logstorage.CachedRandomSeeker;
import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.logstorage.file.LogFileReader;
import org.krakenapps.logstorage.file.LogRecord;

/**
 * not thread-safe
 * 
 * @author xeraph
 * @since 0.9
 */
public class CachedRandomSeekerImpl implements CachedRandomSeeker {
	private boolean closed;
	private LogTableRegistry tableRegistry;
	private LogFileFetcher fetcher;
	private ConcurrentMap<OnlineWriterKey, OnlineWriter> onlineWriters;
	private Map<TabletKey, LogFileReader> cachedReaders;

	public CachedRandomSeekerImpl(LogTableRegistry tableRegistry, LogFileFetcher fetcher,
			ConcurrentMap<OnlineWriterKey, OnlineWriter> onlineWriters) {
		this.tableRegistry = tableRegistry;
		this.fetcher = fetcher;
		this.onlineWriters = onlineWriters;
		this.cachedReaders = new HashMap<TabletKey, LogFileReader>();
	}

	@Override
	public Log getLog(String tableName, Date day, int id) throws IOException {
		if (closed)
			throw new IllegalStateException("already closed");

		int tableId = tableRegistry.getTableId(tableName);

		// check memory buffer (flush waiting)
		OnlineWriter writer = onlineWriters.get(new OnlineWriterKey(tableName, day));
		if (writer != null) {
			for (LogRecord r : writer.getBuffer())
				if (r.getId() == id)
					return new Log(tableName, r.getDate(), id, EncodingRule.decodeMap(r.getData().duplicate()));
		}

		TabletKey key = new TabletKey(tableId, day);
		LogFileReader reader = cachedReaders.get(key);
		if (reader == null) {
			reader = fetcher.fetch(tableName, day);
			cachedReaders.put(key, reader);
		}

		return LogMarshaler.convert(tableName, reader.find(id));
	}

	@Override
	public void close() {
		if (closed)
			return;

		closed = true;

		for (LogFileReader reader : cachedReaders.values()) {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}

		cachedReaders.clear();
	}
}
