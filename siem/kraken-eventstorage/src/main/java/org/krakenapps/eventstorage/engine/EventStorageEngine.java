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
package org.krakenapps.eventstorage.engine;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.api.DateFormat;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.eventstorage.Event;
import org.krakenapps.eventstorage.EventRecord;
import org.krakenapps.eventstorage.EventStorage;
import org.krakenapps.eventstorage.EventStorageStatus;
import org.krakenapps.eventstorage.EventTableNotFoundException;
import org.krakenapps.eventstorage.EventTableRegistry;
import org.krakenapps.eventstorage.engine.GlobalConfig.Key;
import org.krakenapps.eventstorage.engine.file.EventPointerFile;
import org.krakenapps.eventstorage.engine.file.EventReader;
import org.krakenapps.eventstorage.engine.file.EventWriter;
import org.krakenapps.eventstorage.engine.file.FileHandlerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "eventstorage-engine")
@Provides
public class EventStorageEngine implements EventStorage {
	private Logger logger = LoggerFactory.getLogger(EventStorageEngine.class);

	@Requires
	private ConfigService confsvc;

	@Requires
	private EventTableRegistry tableRegistry;

	private File dir;
	private EventStorageStatus status;
	private Map<Integer, AtomicLong> nextIds;

	private FileHandlerManager fileman = new FileHandlerManager(confsvc);
	private Thread filemanThread = new Thread(fileman);

	@Validate
	@Override
	public void start() {
		if (status != null && status != EventStorageStatus.Stopped)
			throw new IllegalStateException("storage not stopped");
		status = EventStorageStatus.Starting;

		String pathname = (String) GlobalConfig.get(confsvc, Key.StorageDirectory);
		setDirectory(new File(pathname));

		this.nextIds = new HashMap<Integer, AtomicLong>();

		@SuppressWarnings("unchecked")
		Map<String, String> savedIds = (Map<String, String>) GlobalConfig.get(confsvc, Key.NextEventId);
		for (String key : savedIds.keySet())
			nextIds.put(Integer.parseInt(key), new AtomicLong(Long.parseLong(savedIds.get(key))));

		filemanThread.start();

		status = EventStorageStatus.Started;
	}

	@Invalidate
	@Override
	public void stop() {
		if (status != EventStorageStatus.Started)
			throw new IllegalStateException("storage not started");
		status = EventStorageStatus.Stopping;

		fileman.setDoStop(true);
		fileman.close();
		filemanThread.interrupt();

		Map<String, String> saveIds = new HashMap<String, String>();
		for (Integer key : nextIds.keySet())
			saveIds.put(key.toString(), String.valueOf(nextIds.get(key).get()));
		GlobalConfig.set(confsvc, Key.NextEventId, saveIds, true);

		try {
			for (int i = 0; i < 25; i++) {
				if (!fileman.isRunning())
					break;
				Thread.sleep(200);
			}
		} catch (InterruptedException e) {
		}

		status = EventStorageStatus.Stopped;
	}

	@Override
	public EventStorageStatus getStatus() {
		return status;
	}

	@Override
	public File getDirectory() {
		return dir;
	}

	@Override
	public File getTableDirectory(String tableName) {
		if (!tableRegistry.exists(tableName))
			throw new EventTableNotFoundException(tableName);

		int tableId = tableRegistry.getTableId(tableName);
		return new File(dir, Integer.toString(tableId));
	}

	@Override
	public void setDirectory(File dir) {
		if (dir == null)
			throw new IllegalArgumentException("storage path should be not null");
		if (!dir.exists())
			dir.mkdirs();
		if (!dir.isDirectory())
			throw new IllegalArgumentException("storage path should be directory");

		GlobalConfig.set(confsvc, Key.StorageDirectory, dir.getAbsolutePath());
		DatapathUtil.setLogDir(dir);
		this.dir = dir;
	}

	@Override
	public void createTable(String tableName) {
		createTable(tableName, null);
	}

	@Override
	public void createTable(String tableName, Map<String, String> tableMetadata) {
		int id = tableRegistry.createTable(tableName, tableMetadata);
		nextIds.put(id, new AtomicLong(1));
	}

	@Override
	public void dropTable(String tableName) {
		int tableId = tableRegistry.getTableId(tableName);
		fileman.close(tableId);
		tableRegistry.dropTable(tableName);

		File dir = DatapathUtil.getDirPath(tableId);
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return DatapathUtil.FileType.isValidFilename(name);
			}
		});
		for (File f : files) {
			if (!f.delete())
				logger.warn("kraken eventstorage: cannot delete event table [" + tableName + "] file [" + f.getAbsolutePath()
						+ "]");
		}
		if (dir.listFiles().length == 0)
			dir.delete();

		nextIds.remove(tableId);
	}

	@Override
	public long getNextId(String tableName) {
		int tableId = tableRegistry.getTableId(tableName);
		AtomicLong id = nextIds.get(tableId);
		if (id == null) {
			id = new AtomicLong(1L);
			nextIds.put(tableId, id);
		}
		return id.getAndIncrement();
	}

	@Override
	public void write(String tableName, EventRecord record) {
		verify();

		try {
			int tableId = tableRegistry.getTableId(tableName);
			AtomicLong id = nextIds.get(tableId);
			if (id == null || id.get() <= record.getId()) {
				id = new AtomicLong(record.getId() + 1);
				nextIds.put(tableId, id);
			}

			EventWriter writer = fileman.getWriter(tableId, record.getDate(), true);
			writer.write(record);
		} catch (IOException e) {
			logger.debug("kraken eventstorage: cannot write event record. table [" + tableName + "]", e);
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void write(String tableName, Collection<EventRecord> records) {
		verify();

		for (EventRecord record : records)
			write(tableName, record);
	}

	@Override
	public void remove(String tableName, long id) {
		try {
			int tableId = tableRegistry.getTableId(tableName);
			EventPointerFile ptr = fileman.getPointerFile(tableId, id, true);
			ptr.remove(id);
		} catch (IOException e) {
			logger.debug("kraken eventstorage: cannot remove event. table [" + tableName + "], id [" + id + "]", e);
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void remove(String tableName, Date day) {
		try {
			int tableId = tableRegistry.getTableId(tableName);
			List<Date> dates = DatapathUtil.getLogDates(tableId);
			Collections.reverse(dates);
			for (Date date : dates) {
				if (date.after(day))
					break;
				fileman.delete(tableId, date);
			}
		} catch (IOException e) {
			logger.debug(
					"kraken eventstorage: cannot remove event. table [" + tableName + "], day ["
							+ DateFormat.format("yyyy-MM-dd", day) + "]", e);
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void flush() {
		fileman.setForceFlush(true);
		filemanThread.interrupt();
	}

	@Override
	public Event getEvent(String tableName, long id) {
		verify();

		int tableId = tableRegistry.getTableId(tableName);
		Iterator<EventReader> it = new EventFileReaderIterator(tableId);
		while (it.hasNext()) {
			EventReader reader = it.next();
			try {
				Event event = reader.read(id);
				if (event != null)
					return event;
			} catch (IOException e) {
				String day = DateFormat.format("yyyy-MM-dd", reader.getDay());
				logger.error("kraken eventstorage: event read error. table [" + tableName + "] day " + day, e);
			} finally {
				if (reader != null)
					reader.close();
			}
		}

		return null;
	}

	@Override
	public Collection<Event> getEvents(String tableName, int limit) {
		verify();
		return getEvents(tableName, 0, limit);
	}

	@Override
	public Collection<Event> getEvents(String tableName, int offset, int limit) {
		verify();

		int tableId = tableRegistry.getTableId(tableName);
		EventReadHelper helper = null;
		try {
			helper = new EventReadHelper(offset, limit);

			Iterator<EventReader> it = new EventFileReaderIterator(tableId);
			while (it.hasNext()) {
				EventReader reader = it.next();
				try {
					if (reader.read(helper))
						break;
				} catch (IOException e) {
					String day = DateFormat.format("yyyy-MM-dd", reader.getDay());
					logger.error("kraken eventstorage: event read error. table [" + tableName + "] day " + day, e);
				} finally {
					if (reader != null)
						reader.close();
				}
			}
		} finally {
			if (helper != null)
				helper.close();
		}
		return helper.getResult();
	}

	private void verify() {
		if (status != EventStorageStatus.Started)
			throw new IllegalStateException("storage not started");
	}

	private class EventFileReaderIterator implements Iterator<EventReader> {
		private int tableId;
		private Iterator<Date> it;

		public EventFileReaderIterator(int tableId) {
			this.tableId = tableId;
			this.it = DatapathUtil.getLogDates(tableId).iterator();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public EventReader next() {
			try {
				return new EventReader(tableId, it.next(), fileman);
			} catch (IOException e) {
				logger.debug("kraken eventstorage: file open failed", e);
				throw new IllegalStateException(e);
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
