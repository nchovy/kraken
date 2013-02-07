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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.krakenapps.api.DateFormat;
import org.krakenapps.eventstorage.Event;
import org.krakenapps.eventstorage.EventRecord;
import org.krakenapps.eventstorage.engine.DatapathUtil;
import org.krakenapps.eventstorage.engine.DatapathUtil.FileType;
import org.krakenapps.eventstorage.engine.EventReadHelper;

public class EventReader {
	private static final int IDX_SIZE = 20;
	private static final int DEFAULT_CACHE_SIZE = 50000;

	private int tableId;
	private Date day;
	private FileHandlerManager fileman;

	private RandomAccessFile idx;
	private EventFileHeader idxhdr;
	private ByteBuffer idxbuf;
	private Object idxlock = new Object();

	private Deque<Long> blocks = new LinkedList<Long>();

	public EventReader(int tableId, Date day, FileHandlerManager fileman) throws IOException {
		this(tableId, day, fileman, DEFAULT_CACHE_SIZE);
	}

	public EventReader(int tableId, Date day, FileHandlerManager fileman, int cacheSize) throws IOException {
		boolean success = false;
		try {
			this.tableId = tableId;
			this.day = day;
			this.fileman = fileman;

			File idxfile = DatapathUtil.getFilePath(tableId, day, FileType.Index);
			this.idx = new RandomAccessFile(idxfile, "r");
			this.idxhdr = getHeader(idxfile, EventFileHeader.MAGIC_STRING_INDEX);
			this.idxbuf = ByteBuffer.allocate(cacheSize * IDX_SIZE + 4);

			this.blocks.add((long) idxhdr.size());
			success = true;
		} finally {
			if (!success)
				close();
		}
	}

	private EventFileHeader getHeader(File f, String magicString) throws IOException {
		EventFileHeader hdr = EventFileHeader.extractHeader(f);
		if (!hdr.magicString().equals(magicString))
			throw new IllegalStateException("invalid magic string " + f.getAbsolutePath());
		if (hdr.version() != 1)
			throw new IllegalStateException("invalid version " + f.getAbsolutePath());
		return hdr;
	}

	public int getTableId() {
		return tableId;
	}

	public Date getDay() {
		return day;
	}

	public Event read(long id) throws IOException {
		EventWriter writer = fileman.getWriter(tableId, day, false);
		if (writer != null) {
			for (EventRecord record : writer.getCache()) {
				if (record.getId() == id)
					return buildEventRecord(id);
			}
		}
		Iterator<Long> it = getIterator();
		while (it.hasNext()) {
			synchronized (idxlock) {
				int l = 0;
				int r = loadIndex(it.next());
				while (l < r) {
					int m = (l + r) / 2;
					idxbuf.position(m * IDX_SIZE + 4);
					long target = idxbuf.getLong();

					if (id == target)
						return buildEventRecord(id);
					else if (id < target)
						r = m;
					else if (id > target)
						l = m + 1;
				}
			}
		}

		return null;
	}

	private Event buildEventRecord(long id) throws IOException {
		Date date = new Date(idxbuf.getLong());
		int count = idxbuf.getInt();

		EventPointerFile ptr = fileman.getPointerFile(tableId, id, false);
		EventRecord record = new EventRecord(id, date, count);
		return (ptr != null) ? ptr.read(record) : null;
	}

	public List<Event> read(int offset, int limit) throws IOException {
		EventReadHelper helper = null;
		try {
			helper = new EventReadHelper(offset, limit);
			read(helper);
		} finally {
			if (helper != null)
				helper.close();
		}
		return helper.getResult();
	}

	public boolean read(EventReadHelper helper) throws IOException {
		if (helper.getLimit() == 0)
			return true;

		EventWriter writer = fileman.getWriter(tableId, day, false);
		if (writer != null) {
			for (EventRecord record : writer.getCache()) {
				EventPointerFile ptr = fileman.getPointerFile(tableId, record.getId(), false);
				if (ptr == null || !ptr.isValid(record.getId()))
					continue;

				if (helper.addReadedId(record.getId())) {
					if (addResult(helper, record))
						return true;
				}
			}
		}

		Iterator<Long> it = getIterator();
		while (it.hasNext()) {
			List<EventRecord> records = null;
			synchronized (idxlock) {
				int cnt = loadIndex(it.next());
				records = new ArrayList<EventRecord>(cnt);
				for (int i = 0; i < cnt; i++) {
					long id = idxbuf.getLong();
					long modified = idxbuf.getLong();
					int count = idxbuf.getInt();

					EventPointerFile ptr = fileman.getPointerFile(tableId, id, false);
					if (ptr == null || !ptr.isValid(id))
						continue;

					if (helper.addReadedId(id))
						records.add(new EventRecord(id, new Date(modified), count));
				}
			}

			if (helper.getOffset() >= helper.getHits() + records.size()) {
				helper.incrementHits(records.size());
				continue;
			}

			Collections.sort(records, new Comparator<EventRecord>() {
				@Override
				public int compare(EventRecord o1, EventRecord o2) {
					return o2.getDate().compareTo(o1.getDate());
				}
			});

			for (EventRecord record : records) {
				if (addResult(helper, record))
					return true;
			}
		}

		return false;
	}

	private int loadIndex(long fp) throws IOException {
		idx.seek(fp);
		int len = idx.read(idxbuf.array());
		idxbuf.clear();
		int cnt = idxbuf.getInt();

		if (len < cnt * IDX_SIZE + 4) {
			if (idxbuf.array().length < cnt * IDX_SIZE + 4) {
				idxbuf = ByteBuffer.allocate(cnt * IDX_SIZE + 4);

				idx.seek(fp);
				len = idx.read(idxbuf.array());
				idxbuf.clear();
				cnt = idxbuf.getInt();
			}

			if (len < cnt * IDX_SIZE + 4)
				throw new IOException("event read error " + toString());
		}
		idxbuf.limit(cnt * IDX_SIZE + 4);

		return cnt;
	}

	private boolean addResult(EventReadHelper helper, EventRecord r) throws IOException {
		if (helper.incrementHits(1) <= helper.getOffset())
			return false;

		EventPointerFile ptr = fileman.getPointerFile(tableId, r.getId(), false);
		Event event = (ptr != null) ? ptr.read(r) : null;

		if (event == null)
			return false;

		helper.addResult(event);
		return (helper.getResult().size() >= helper.getLimit());
	}

	private Iterator<Long> getIterator() throws IOException {
		if (idx.length() <= idxhdr.size())
			return new ArrayList<Long>().iterator();

		long fp = blocks.getFirst();
		while (idx.length() > fp) {
			idx.seek(fp);
			fp += idx.readInt() * IDX_SIZE + 4;
			blocks.addFirst(fp);
		}

		Iterator<Long> it = blocks.iterator();
		it.next();
		return it;
	}

	public void close() {
		try {
			if (idx != null)
				idx.close();
		} catch (IOException e) {
		}
	}

	@Override
	public String toString() {
		return "EventFileReader [tableId=" + tableId + ", day=" + DateFormat.format("yyyy-MM-dd", day) + "]";
	}
}
