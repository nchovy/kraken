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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.krakenapps.eventstorage.Event;
import org.krakenapps.eventstorage.EventRecord;
import org.krakenapps.eventstorage.engine.BufferedRandomAccessFile;
import org.krakenapps.eventstorage.engine.DatapathUtil;

public class EventPointerFile extends FileWriter {
	private static final long TIMEZONE_OFFSET = Calendar.getInstance().getTimeZone().getRawOffset();
	private static final int PTR_SIZE = 10;
	private static final int FLG_SIZE = 0x1000000 >> 3;
	private static final int DEFAULT_CACHE_SIZE = 50000;

	private int key;
	private FileHandlerManager fileman;
	private boolean write;

	private EventFileHeader ptrhdr;
	private BufferedRandomAccessFile ptrbuf;
	private Object ptrlock = new Object();

	private Map<Integer, Pointer> cache;
	private int cacheSize;
	private Object cachelock = new Object();

	private byte[] flags = new byte[FLG_SIZE];
	private int invalidFlags = 0;

	public EventPointerFile(int tableId, int key, FileHandlerManager fileman, boolean write) throws IOException {
		this(tableId, key, fileman, true, DEFAULT_CACHE_SIZE);
	}

	public EventPointerFile(int tableId, int key, FileHandlerManager fileman, boolean write, int cacheSize) throws IOException {
		super(tableId, new File(DatapathUtil.getDirPath(tableId), "data" + key + DatapathUtil.FileType.Pointer));

		boolean success = false;
		try {
			this.key = key;
			this.fileman = fileman;
			this.write = write;

			this.ptrhdr = getHeader(EventFileHeader.MAGIC_STRING_POINTER);
			if (!getFile().exists() && !write)
				throw new FileNotFoundException();
			if (getFile().length() == ptrhdr.size()) {
				byte[] b = Arrays.copyOf(ptrhdr.serialize(), ptrhdr.size() + FLG_SIZE);
				Arrays.fill(b, ptrhdr.size(), b.length, (byte) 0xFF);
				FileOutputStream fos = new FileOutputStream(getFile());
				fos.write(b);
				fos.close();
			}
			this.ptrbuf = new BufferedRandomAccessFile(getFile(), write ? "rw" : "r", 4096, ptrhdr.size());
			this.ptrbuf.seek(0L);
			this.ptrbuf.read(flags);
			for (byte b : flags) {
				if (b == (byte) 0xFF)
					invalidFlags++;
			}

			this.cache = new TreeMap<Integer, Pointer>();
			this.cacheSize = cacheSize;

			success = true;
		} finally {
			if (!success)
				close();
		}
	}

	public int getKey() {
		return key;
	}

	public Event read(EventRecord record) throws IOException {
		touch();

		int id = (int) (record.getId() & 0xFFFFFF);
		Pointer p = cache.get(id);
		if (p != null) {
			EventDataFile dat = fileman.getDataFile(getTableId(), getDay(p.day), false);
			return (dat != null) ? dat.read(p.fp, record) : null;
		}

		if (!isValid(id))
			return null;

		short day = 0;
		long fp = 0;
		synchronized (ptrlock) {
			ptrbuf.seek(FLG_SIZE + id * PTR_SIZE);
			day = ptrbuf.readShort();
			fp = ptrbuf.readLong();
		}

		EventDataFile dat = fileman.getDataFile(getTableId(), getDay(day), false);
		return (dat != null) ? dat.read(fp, record) : null;
	}

	public boolean isValid(long id) {
		int pos = (int) (id & 0xFFFFFF) >>> 3;
		return ((flags[pos] & 0xFF) & (0x80 >>> (id & 0x7))) == 0;
	}

	private void setValidFlag(long id, boolean valid) {
		modify();
		int pos = (int) (id & 0xFFFFFF) >>> 3;
		if (valid) {
			if (flags[pos] == (byte) 0xFF)
				invalidFlags--;
			flags[pos] = (byte) ((flags[pos] & 0xFF) & ~(0x80 >>> (id & 0x7)));
		} else {
			boolean invalid = (flags[pos] == (byte) 0xFF);
			flags[pos] = (byte) ((flags[pos] & 0xFF) | (0x80 >>> (id & 0x7)));
			if (!invalid && flags[pos] == (byte) 0xFF)
				invalidFlags++;
		}
	}

	private Date getDay(short day) {
		return new Date(((long) day & 0xFFFFL) * 86400000L - TIMEZONE_OFFSET);
	}

	public void remove(long id) throws IOException {
		remove(id, null);
	}

	public void remove(long id, Short day) throws IOException {
		if ((int) ((id >>> 24) & 0xFFFFFF) != key)
			return;

		int pos = (int) (id & 0xFFFFFF);
		if (cache.containsKey(pos))
			return;

		synchronized (ptrlock) {
			ptrbuf.seek(FLG_SIZE + id * PTR_SIZE);
			if (day != null && day != ptrbuf.readShort())
				return;
		}
		setValidFlag(id, false);

		if (invalidFlags == FLG_SIZE)
			close();
	}

	@Override
	protected void doWrite(EventRecord record) throws IOException {
		if (!write) {
			write = true;
			ptrbuf.close();
			ptrbuf = new BufferedRandomAccessFile(getFile(), "rw", 4096, ptrhdr.size());
		}

		int id = (int) (record.getId() & 0xFFFFFF);
		if (!record.isUpdateData() && record.getData() == null) {
			if (isValid(record.getId()))
				return;
		}

		setValidFlag(id, true);
		short day = (short) ((record.getDate().getTime() + TIMEZONE_OFFSET) / 86400000L);
		EventDataFile dat = fileman.getDataFile(getTableId(), record.getDate(), true);
		long fp = dat.writeAndGetFp(record);
		synchronized (cachelock) {
			cache.put(id, new Pointer(id, day, fp));
		}

		if (cache.size() >= cacheSize)
			flush(true);
	}

	protected void doFlush(boolean sync) throws IOException {
		List<Pointer> ptrs = null;
		synchronized (cachelock) {
			ptrs = new ArrayList<Pointer>(cache.values());
			cache.clear();
		}

		synchronized (ptrlock) {
			ptrbuf.seek(0L);
			ptrbuf.write(flags);

			for (Pointer p : ptrs) {
				ptrbuf.seek(FLG_SIZE + p.id * PTR_SIZE);
				ptrbuf.writeShort(p.day);
				ptrbuf.writeLong(p.fp);
			}
		}

		ptrbuf.flush(sync);
	}

	@Override
	protected void doClose() {
		if (ptrbuf != null)
			ptrbuf.close();

		if (invalidFlags == FLG_SIZE)
			getFile().delete();
	}

	private class Pointer {
		private int id;
		private short day;
		private long fp;

		private Pointer(int id, short day, long fp) {
			this.id = id;
			this.day = day;
			this.fp = fp;
		}
	}

	@Override
	public String toString() {
		return "EventPointerFile [tableId=" + getTableId() + ", key=" + key + "]";
	}
}
