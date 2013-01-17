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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.krakenapps.logstorage.LogIndexCursor;
import org.krakenapps.logstorage.LogIndexItem;
import org.krakenapps.logstorage.index.InvertedIndexCursor;
import org.krakenapps.logstorage.index.InvertedIndexItem;
import org.krakenapps.logstorage.index.InvertedIndexReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 0.9
 * @author xeraph
 */
class IndexCursorImpl implements LogIndexCursor {
	private final Logger logger = LoggerFactory.getLogger(IndexCursorImpl.class);
	private int indexId;
	private int tableId;
	private String tableName;

	// sorted indexed days (from latest to oldest)
	private List<Date> days;

	private File indexBaseDir;

	private String term;

	// loading index (point to days)
	private int current;
	private int dayCount;

	private Date currentDay;
	private InvertedIndexReader currentReader;
	private InvertedIndexCursor currentCursor;
	private Iterator<InvertedIndexItem> buffer;

	private Long prefetch;

	public IndexCursorImpl(int indexId, int tableId, String tableName, List<Date> days, String term, File indexBaseDir,
			Iterator<InvertedIndexItem> buffer) throws IOException {
		this.indexId = indexId;
		this.tableId = tableId;
		this.tableName = tableName;
		this.days = days;
		this.dayCount = days.size();
		this.term = term;
		this.indexBaseDir = indexBaseDir;
		this.buffer = buffer;

		if (days.size() > 0) {
			try {
				load(days.get(0));
			} catch (Throwable t) {
				logger.error("kraken logstorage: cannot load index file, skipping", t);
				tryLoadUntilSuccess();
			}
		}
	}

	private void tryLoadUntilSuccess() {
		while (true) {
			try {
				loadNext();
				break;
			} catch (Throwable t) {
				logger.error("kraken logstorage: cannot load index file, skipping", t);
			}
		}
	}

	private boolean loadNext() throws IOException {
		if (current >= dayCount - 1)
			return false;

		load(days.get(++current));
		return true;
	}

	private void load(Date day) throws IOException {
		File indexFile = getIndexFilePath(tableId, indexId, day, ".pos");
		File dataFile = getIndexFilePath(tableId, indexId, day, ".seg");

		currentDay = day;
		currentReader = new InvertedIndexReader(indexFile, dataFile);
		currentCursor = currentReader.openCursor(term);
	}

	@Override
	public boolean hasNext() {
		if (buffer != null) {
			boolean ret = buffer.hasNext();
			if (ret)
				return true;
			else
				buffer = null;
		}

		if (prefetch != null)
			return true;

		// no index files
		if (currentCursor == null)
			return false;

		if (currentCursor.hasNext()) {
			try {
				prefetch = currentCursor.next();
				return true;
			} catch (IOException e) {
				logger.error("kraken logstorage: cannot fetch next index item from cursor", e);
			}
		}

		currentReader.close();

		try {
			if (loadNext())
				return hasNext();
		} catch (IOException e) {
			logger.error("kraken logstorage: cannot load next indexed day from cursor", e);
		}

		return false;
	}

	private File getIndexFilePath(int tableId, int indexId, Date day, String suffix) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String relativePath = tableId + "/" + indexId + "/" + dateFormat.format(day) + suffix;
		return new File(indexBaseDir, relativePath);
	}

	@Override
	public LogIndexItem next() {
		if (!hasNext())
			throw new NoSuchElementException("no more indexed log id");

		if (buffer != null && buffer.hasNext()) {
			InvertedIndexItem item = buffer.next();
			return new IndexItem(item.tableName, DateUtil.getDay(new Date(item.timestamp)), item.id);
		}

		Long ret = prefetch;
		prefetch = null;
		return new IndexItem(tableName, currentDay, ret);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void skip(long offset) {
	}

	@Override
	public void close() {
		buffer = null;
	}

	private static class IndexItem implements LogIndexItem {
		private String tableName;
		private Date day;
		private long id;

		public IndexItem(String tableName, Date day, long id) {
			this.tableName = tableName;
			this.day = day;
			this.id = id;
		}

		@Override
		public String getTableName() {
			return tableName;
		}

		@Override
		public Date getDay() {
			return day;
		}

		@Override
		public long getLogId() {
			return id;
		}

		@Override
		public String toString() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			return "index item, table=" + tableName + ", day=" + dateFormat.format(day) + ", id=" + id;
		}
	}
}