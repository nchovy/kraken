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
package org.krakenapps.eventstorage;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public interface EventStorage {
	File getDirectory();

	File getTableDirectory(String tableName);

	void setDirectory(File dir);

	void start();

	void stop();
	
	EventStorageStatus getStatus();

	void createTable(String tableName);

	void createTable(String tableName, Map<String, String> tableMetadata);

	void dropTable(String tableName);

	long getNextId(String tableName);

	void write(String tableName, EventRecord record);

	void write(String tableName, Collection<EventRecord> records);

	void remove(String tableName, long id);

	void remove(String tableName, Date day);

	void flush();
	
	Event getEvent(String tableName, long id);

	Collection<Event> getEvents(String tableName, int limit);

	Collection<Event> getEvents(String tableName, int offset, int limit);
}
