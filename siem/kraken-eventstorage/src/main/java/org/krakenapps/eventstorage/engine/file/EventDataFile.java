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
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.krakenapps.api.DateFormat;
import org.krakenapps.eventstorage.Event;
import org.krakenapps.eventstorage.EventRecord;
import org.krakenapps.eventstorage.engine.BufferedRandomAccessFile;
import org.krakenapps.eventstorage.engine.DatapathUtil;
import org.krakenapps.eventstorage.engine.DatapathUtil.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventDataFile extends FileWriter {
	private static final int DAT_SIZE = 21;
	private static final int DEFAULT_CACHE_SIZE = 16 * 1024 * 1024;

	private Logger logger = LoggerFactory.getLogger(EventDataFile.class);
	private Date day;
	private boolean write;

	private EventFileHeader dathdr;
	private BufferedRandomAccessFile datbuf;
	private Object datlock = new Object();

	private ByteBuffer cache;
	private Object cachelock = new Object();

	public EventDataFile(int tableId, Date day, boolean write) throws IOException {
		this(tableId, day, true, DEFAULT_CACHE_SIZE);
	}

	public EventDataFile(int tableId, Date day, boolean write, int cacheSize) throws IOException {
		super(tableId, DatapathUtil.getFilePath(tableId, day, FileType.Data));

		boolean success = false;
		try {
			this.day = day;
			this.write = write;

			this.dathdr = getHeader(EventFileHeader.MAGIC_STRING_DATA);
			this.datbuf = new BufferedRandomAccessFile(getFile(), write ? "rw" : "r", 524288, dathdr.size());

			this.cache = ByteBuffer.allocate(cacheSize);

			success = true;
		} finally {
			if (!success)
				close();
		}
	}

	public Date getDay() {
		return day;
	}

	public Event read(long fp, EventRecord record) throws IOException {
		touch();

		if (fp < datbuf.length()) {
			synchronized (datlock) {
				datbuf.seek(fp);
				long id = datbuf.readLong();
				if (id != record.getId())
					logger.warn("kraken eventstorage: invalid data. fp [{}], record id [{}], data id [{}]", new Object[] { fp,
							record.getId(), id });
				Date created = new Date(datbuf.readLong());
				int datlen = datbuf.readInt();
				byte reserved = datbuf.readByte();
				byte[] data = new byte[datlen];
				datbuf.read(data);
				return new Event(getTableId(), record.getId(), created, record.getDate(), record.getCount(), reserved, data);
			}
		} else {
			synchronized (cachelock) {
				int pos = cache.position();
				cache.position((int) (fp - datbuf.length()));
				long id = cache.getLong();
				if (id != record.getId())
					logger.warn("kraken eventstorage: invalid data. fp [{}], record id [{}], data id [{}]", new Object[] { fp,
							record.getId(), id });
				Date created = new Date(cache.getLong());
				int datlen = cache.getInt();
				byte reserved = cache.get();
				byte[] data = new byte[datlen];
				cache.get(data);
				cache.position(pos);
				return new Event(getTableId(), record.getId(), created, record.getDate(), record.getCount(), reserved, data);
			}
		}
	}

	public Set<Long> getIds() throws IOException {
		Set<Long> ids = new HashSet<Long>();
		long fp = 0L;
		synchronized (datlock) {
			while (fp < datbuf.length()) {
				datbuf.seek(fp);
				long id = datbuf.readLong();
				ids.add(id);
				datbuf.readLong();
				fp += DAT_SIZE + datbuf.readInt();
			}
		}
		return ids;
	}

	public long writeAndGetFp(EventRecord record) throws IOException {
		synchronized (datlock) {
			long fp = datbuf.length() + cache.position();
			write(record);
			return fp;
		}
	}

	@Override
	protected void doWrite(EventRecord record) throws IOException {
		if (!write) {
			write = true;
			datbuf.close();
			datbuf = new BufferedRandomAccessFile(getFile(), "rw", 524288, dathdr.size());
		}

		synchronized (cachelock) {
			if (cache.remaining() < record.getDataLength() + DAT_SIZE)
				flush(true);

			cache.putLong(record.getId());
			cache.putLong(record.getDate().getTime());
			cache.putInt(record.getDataLength());
			cache.put((byte) 0x00); // reserved
			cache.put((record.getData() != null) ? record.getData() : new byte[0]);
		}
	}

	@Override
	protected void doFlush(boolean sync) throws IOException {
		synchronized (datlock) {
			datbuf.seek(datbuf.length());
			datbuf.write(cache.array(), 0, cache.position());
			cache.clear();

			datbuf.flush(sync);
		}
	}

	@Override
	protected void doClose() {
		if (datbuf != null)
			datbuf.close();
	}

	@Override
	public String toString() {
		return "EventDataFile [tableId=" + getTableId() + ", day=" + DateFormat.format("yyyy-MM-dd", day) + "]";
	}
}
