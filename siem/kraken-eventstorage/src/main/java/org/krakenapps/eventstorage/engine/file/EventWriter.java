/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.eventstorage.engine.file;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.krakenapps.api.DateFormat;
import org.krakenapps.eventstorage.EventRecord;
import org.krakenapps.eventstorage.engine.DatapathUtil;
import org.krakenapps.eventstorage.engine.DatapathUtil.FileType;

public class EventWriter extends FileWriter {
	private static final int IDX_SIZE = 20;
	private static final int DEFAULT_CACHE_SIZE = 50000;

	private Date day;
	private FileHandlerManager fileman;

	private RandomAccessFile idx;
	@SuppressWarnings("unused")
	private EventFileHeader idxhdr;
	private ByteBuffer idxbuf;

	private Set<EventRecord> cache;
	private int cacheSize;
	private Object cachelock = new Object();

	public EventWriter(int tableId, Date day, FileHandlerManager fileman) throws IOException {
		this(tableId, day, fileman, DEFAULT_CACHE_SIZE);
	}

	public EventWriter(int tableId, Date day, FileHandlerManager fileman, int cacheSize) throws IOException {
		super(tableId, DatapathUtil.getFilePath(tableId, day, FileType.Index));

		boolean success = false;
		try {
			this.day = day;
			this.fileman = fileman;

			this.idx = new RandomAccessFile(getFile(), "rw");
			this.idxhdr = getHeader(EventFileHeader.MAGIC_STRING_INDEX);
			this.idxbuf = ByteBuffer.allocate(cacheSize * IDX_SIZE + 4);
			idx.seek(idx.length());

			this.cache = new TreeSet<EventRecord>(new Comparator<EventRecord>() {
				@Override
				public int compare(EventRecord o1, EventRecord o2) {
					return (int) (o1.getId() - o2.getId());
				}
			});
			this.cacheSize = cacheSize;

			success = true;
		} finally {
			if (!success)
				close();
		}
	}

	public Date getDay() {
		return day;
	}

	@Override
	protected void doWrite(EventRecord record) throws IOException {
		synchronized (cachelock) {
			cache.add(record);
		}

		EventPointerFile ptr = fileman.getPointerFile(getTableId(), record.getId(), true);
		ptr.write(record);

		if (cache.size() >= cacheSize)
			flush(true);
	}

	public List<EventRecord> getCache() {
		ArrayList<EventRecord> records = null;
		synchronized (cachelock) {
			records = new ArrayList<EventRecord>(cache);
		}
		Collections.sort(records, new Comparator<EventRecord>() {
			@Override
			public int compare(EventRecord o1, EventRecord o2) {
				return o2.getDate().compareTo(o1.getDate());
			}
		});
		return records;
	}

	@Override
	protected void doFlush(boolean sync) throws IOException {
		List<EventRecord> idxs = null;
		if (sync) {
			synchronized (cachelock) {
				idxs = new ArrayList<EventRecord>(cache);
				cache.clear();
			}
		} else {
			idxs = new ArrayList<EventRecord>(cache);
		}

		idxbuf.clear();
		idxbuf.putInt(idxs.size());
		for (EventRecord index : idxs) {
			idxbuf.putLong(index.getId());
			idxbuf.putLong(index.getDate().getTime());
			idxbuf.putInt(index.getCount());
		}

		long before = idx.getFilePointer();
		idx.write(idxbuf.array(), 0, idxs.size() * IDX_SIZE + 4);

		if (sync)
			idx.getFD().sync();
		else
			idx.seek(before);
	}

	@Override
	protected void doClose() {
		try {
			if (idx != null)
				idx.close();
		} catch (IOException e) {
		}
	}

	@Override
	public String toString() {
		return "EventWriter [tableId=" + getTableId() + ", day=" + DateFormat.format("yyyy-MM-dd", day) + "]";
	}
}
