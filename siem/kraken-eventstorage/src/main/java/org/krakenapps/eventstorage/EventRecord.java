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

import java.util.Date;

public class EventRecord {
	private long id; // only use 6byte (max 281,474,976,710,656)
	private Date date;
	private int count;
	private byte[] data;
	private boolean updateData;

	public EventRecord(long id, Date date, int count) {
		this(id, date, count, null);
	}

	public EventRecord(long id, Date date, int count, byte[] data) {
		this(id, date, count, data, false);
	}

	public EventRecord(long id, Date date, int count, byte[] data, boolean updateData) {
		this.id = id;
		this.date = date;
		this.count = count;
		this.data = data;
		this.updateData = updateData;
	}

	public long getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public int getCount() {
		return count;
	}

	public byte[] getData() {
		return data;
	}

	public int getDataLength() {
		return (data != null) ? data.length : 0;
	}

	public boolean isUpdateData() {
		return updateData;
	}

	@Override
	public String toString() {
		return "EventRecord [id=" + id + ", date=" + date + ", count=" + count + ", data_size=" + getDataLength()
				+ ", updateData=" + updateData + "]";
	}
}
