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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.krakenapps.confdb.ConfigService;
import org.krakenapps.eventstorage.engine.DatapathUtil;
import org.krakenapps.eventstorage.engine.DatapathUtil.FileType;
import org.krakenapps.eventstorage.engine.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHandlerManager implements Runnable {
	private static final long TIMEZONE_OFFSET = Calendar.getInstance().getTimeZone().getRawOffset();

	private Logger logger = LoggerFactory.getLogger(FileHandlerManager.class);
	private ConcurrentMap<DayKey, EventWriter> idxs;
	private ConcurrentMap<IntKey, EventPointerFile> ptrs;
	private ConcurrentMap<DayKey, EventDataFile> dats;

	private ConfigService confsvc;

	private volatile int checkInterval;
	private volatile int flushInterval;
	private volatile int maxIdleTime;

	private volatile boolean doStop;
	private volatile boolean isRunning;
	private volatile boolean forceFlush;

	public FileHandlerManager(ConfigService confsvc) {
		this.idxs = new ConcurrentHashMap<DayKey, EventWriter>();
		this.ptrs = new ConcurrentHashMap<IntKey, EventPointerFile>();
		this.dats = new ConcurrentHashMap<DayKey, EventDataFile>();
		this.confsvc = confsvc;
	}

	@Override
	public void run() {
		try {
			isRunning = true;

			this.checkInterval = Integer.parseInt(GlobalConfig.get(confsvc, GlobalConfig.Key.CheckInterval).toString());
			this.flushInterval = Integer.parseInt(GlobalConfig.get(confsvc, GlobalConfig.Key.FlushInterval).toString());
			this.maxIdleTime = Integer.parseInt(GlobalConfig.get(confsvc, GlobalConfig.Key.MaxIdleTime).toString());

			while (true) {
				try {
					try {
						if (doStop)
							break;

						Thread.sleep(checkInterval);
						sweep(false);
					} catch (InterruptedException e) {
						if (forceFlush) {
							sweep(true);
							forceFlush = false;
						}
					}
				} catch (Throwable e) {
					logger.error("kraken eventstorage: sweeper error", e);
				}
			}
		} finally {
			doStop = false;
			isRunning = false;
		}

		logger.info("kraken eventstorage: writer sweeper stopped");
	}

	private void sweep(boolean force) {
		Iterator<DayKey> datit = dats.keySet().iterator();
		while (datit.hasNext())
			sweep(force, dats.get(datit.next()), datit);

		Iterator<IntKey> ptrit = ptrs.keySet().iterator();
		while (ptrit.hasNext())
			sweep(force, ptrs.get(ptrit.next()), ptrit);

		Iterator<DayKey> idxit = idxs.keySet().iterator();
		while (idxit.hasNext())
			sweep(force, idxs.get(idxit.next()), idxit);
	}

	private void sweep(boolean force, FileWriter writer, Iterator<?> it) {
		long current = System.currentTimeMillis();

		if (!writer.isClosed() && (force || current - writer.getLastFlushTime().getTime() > flushInterval)) {
			try {
				writer.flush(true);
			} catch (IOException e) {
				logger.warn("kraken eventstorage: event flush failed", e);
			}
		}

		if (writer.isClosed() || current - writer.getLastWriteTime().getTime() > maxIdleTime) {
			it.remove();
			writer.close();
		}
	}

	public EventWriter getWriter(int tableId, Date date, boolean create) {
		long time = date.getTime();
		date = new Date(time - ((time + TIMEZONE_OFFSET) % 86400000L));

		DayKey key = new DayKey(tableId, date);
		EventWriter writer = idxs.get(key);
		if (!create)
			return (writer == null || writer.isClosed()) ? null : writer;

		if (writer == null || writer.isClosed()) {
			try {
				writer = new EventWriter(tableId, date, this);
				EventWriter old = idxs.putIfAbsent(key, writer);
				if (old != null && !old.isClosed()) {
					writer.close();
					return old;
				}
			} catch (IOException e) {
				logger.debug("kraken eventstorage: cannot create event file writer.", e);
				throw new IllegalStateException(e);
			}
		}
		return writer;
	}

	public EventPointerFile getPointerFile(int tableId, long id, boolean write) {
		int key = (int) ((id >>> 24) & 0xFFFFFF);
		IntKey k = new IntKey(tableId, key);
		EventPointerFile ptr = ptrs.get(k);
		if (ptr == null || ptr.isClosed()) {
			try {
				ptr = new EventPointerFile(k.tableId, k.key, this, write);
				EventPointerFile old = ptrs.putIfAbsent(k, ptr);
				if (old != null && !old.isClosed()) {
					ptr.close();
					return old;
				}
			} catch (FileNotFoundException e) {
				return null;
			} catch (IOException e) {
				logger.debug("kraken eventstorage: cannot create event file writer.", e);
				throw new IllegalStateException(e);
			}
		}
		return ptr;
	}

	public EventDataFile getDataFile(int tableId, Date day, boolean write) {
		long time = day.getTime();
		day = new Date(time - ((time + TIMEZONE_OFFSET) % 86400000L));

		DayKey key = new DayKey(tableId, day);
		EventDataFile dat = dats.get(key);
		if (dat == null || dat.isClosed()) {
			try {
				dat = new EventDataFile(key.tableId, key.day, write);
				EventDataFile old = dats.putIfAbsent(key, dat);
				if (old != null && !dat.isClosed()) {
					dat.close();
					return old;
				}
			} catch (FileNotFoundException e) {
				return null;
			} catch (IOException e) {
				logger.debug("kraken eventstorage: cannot create event file writer.", e);
				throw new IllegalStateException(e);
			}
		}
		return dat;
	}

	public void delete(int tableId, Date day) throws IOException {
		long time = day.getTime();
		day = new Date(time - ((time + TIMEZONE_OFFSET) % 86400000L));
		DayKey key = new DayKey(tableId, day);

		EventWriter idx = idxs.remove(key);
		if (idx != null)
			idx.close();
		DatapathUtil.getFilePath(tableId, day, FileType.Index).delete();

		EventDataFile dat = dats.remove(key);
		if (dat == null)
			dat = new EventDataFile(tableId, day, false);
		Set<Long> ids = dat.getIds();
		dat.close();
		DatapathUtil.getFilePath(tableId, day, FileType.Data).delete();

		for (long id : ids) {
			EventPointerFile ptr = getPointerFile(tableId, (int) ((id >>> 24) & 0xFFFFFF), true);
			ptr.remove(id, (short) ((day.getTime() + TIMEZONE_OFFSET) / 86400000L));
		}
	}

	public void close(int tableId) {
		Iterator<DayKey> datit = dats.keySet().iterator();
		while (datit.hasNext()) {
			DayKey key = datit.next();
			if (key.tableId == tableId) {
				EventDataFile dat = dats.get(key);
				datit.remove();
				dat.close();
			}
		}

		Iterator<IntKey> ptrit = ptrs.keySet().iterator();
		while (ptrit.hasNext()) {
			IntKey key = ptrit.next();
			if (key.tableId == tableId) {
				EventPointerFile ptr = ptrs.get(key);
				ptrit.remove();
				ptr.close();
			}
		}

		Iterator<DayKey> idxit = idxs.keySet().iterator();
		while (idxit.hasNext()) {
			DayKey key = idxit.next();
			if (key.tableId == tableId) {
				EventWriter idx = idxs.get(key);
				idxit.remove();
				idx.close();
			}
		}
	}

	public void close() {
		Collection<EventDataFile> datfiles = new ArrayList<EventDataFile>(dats.values());
		dats.clear();
		for (EventDataFile dat : datfiles)
			dat.close();

		Collection<EventPointerFile> ptrfiles = new ArrayList<EventPointerFile>(ptrs.values());
		ptrs.clear();
		for (EventPointerFile ptr : ptrfiles)
			ptr.close();

		Collection<EventWriter> idxfiles = new ArrayList<EventWriter>(idxs.values());
		idxs.clear();
		for (EventWriter idx : idxfiles)
			idx.close();
	}

	public int getCheckInterval() {
		return checkInterval;
	}

	public void setCheckInterval(int checkInterval) {
		GlobalConfig.set(confsvc, GlobalConfig.Key.CheckInterval, checkInterval);
		this.checkInterval = checkInterval;
	}

	public int getFlushInterval() {
		return flushInterval;
	}

	public void setFlushInterval(int flushInterval) {
		GlobalConfig.set(confsvc, GlobalConfig.Key.FlushInterval, flushInterval);
		this.flushInterval = flushInterval;
	}

	public int getMaxIdleTime() {
		return maxIdleTime;
	}

	public void setMaxIdleTime(int maxIdleTime) {
		GlobalConfig.set(confsvc, GlobalConfig.Key.MaxIdleTime, maxIdleTime);
		this.maxIdleTime = maxIdleTime;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setDoStop(boolean doStop) {
		this.doStop = doStop;
	}

	public void setForceFlush(boolean forceFlush) {
		this.forceFlush = forceFlush;
	}

	private class DayKey {
		private int tableId;
		private Date day;

		public DayKey(int tableId, Date day) {
			this.tableId = tableId;
			this.day = day;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((day == null) ? 0 : day.hashCode());
			result = prime * result + tableId;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DayKey other = (DayKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (day == null) {
				if (other.day != null)
					return false;
			} else if (!day.equals(other.day))
				return false;
			if (tableId != other.tableId)
				return false;
			return true;
		}

		private FileHandlerManager getOuterType() {
			return FileHandlerManager.this;
		}
	}

	private class IntKey {
		private int tableId;
		private int key;

		private IntKey(int tableId, int key) {
			this.tableId = tableId;
			this.key = key;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + key;
			result = prime * result + tableId;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IntKey other = (IntKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (key != other.key)
				return false;
			if (tableId != other.tableId)
				return false;
			return true;
		}

		private FileHandlerManager getOuterType() {
			return FileHandlerManager.this;
		}
	}
}