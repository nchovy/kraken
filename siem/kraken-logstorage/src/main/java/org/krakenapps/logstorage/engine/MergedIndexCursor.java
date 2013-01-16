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
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.krakenapps.logstorage.LogIndexCursor;
import org.krakenapps.logstorage.LogIndexItem;
import org.krakenapps.logstorage.LogIndexQuery;
import org.krakenapps.logstorage.LogIndexer;

class MergedIndexCursor implements LogIndexCursor {
	private LogIndexer indexer;
	private LogIndexQuery query;
	private List<IndexCursorItem> items;
	private File indexBaseDir;

	/**
	 * from 0 to (items.size - 1)
	 */
	private int currentIndex;
	private IndexCursorImpl currentCursor;

	public MergedIndexCursor(LogIndexer indexer, LogIndexQuery query, List<IndexCursorItem> items, File indexBaseDir)
			throws IOException {
		this.indexer = indexer;
		this.query = query;
		this.items = items;
		this.indexBaseDir = indexBaseDir;

		if (!items.isEmpty())
			load(0);
	}

	private boolean loadNext() throws IOException {
		if (currentCursor != null) {
			currentCursor.close();
			currentCursor = null;
		}

		if (currentIndex >= items.size() - 1)
			return false;

		load(++currentIndex);
		return true;
	}

	private void load(int index) throws IOException {
		IndexCursorItem item = items.get(index);
		List<Date> totalDays = indexer.getIndexedDays(item.tableName, item.indexName);
		List<Date> filtered = DateUtil.filt(totalDays, query.getMinDay(), query.getMaxDay());
		DateUtil.sortByDesc(filtered);

		currentCursor = new IndexCursorImpl(item.indexId, item.tableId, item.tableName, filtered, query.getTerm(), indexBaseDir,
				item.buffer.iterator());
	}

	@Override
	public boolean hasNext() {
		if (currentIndex >= items.size())
			return false;

		if (currentCursor == null)
			return false;

		if (currentCursor.hasNext())
			return true;

		try {
			boolean ret = loadNext();
			if (ret)
				return currentCursor.hasNext();
		} catch (IOException e) {
		}
		return false;
	}

	@Override
	public LogIndexItem next() {
		if (!hasNext())
			throw new NoSuchElementException();

		return currentCursor.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("not allowed action");
	}

	@Override
	public void skip(long offset) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void close() {
		if (currentCursor != null)
			currentCursor.close();
	}
}
