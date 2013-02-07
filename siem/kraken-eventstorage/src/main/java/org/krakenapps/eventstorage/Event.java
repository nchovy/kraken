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

public class Event {
	private int tableId;
	private long id;
	private Date created;
	private Date modified;
	private int count;
	private byte reserved;
	private byte[] data;

	public Event(int tableId, long id, Date created, Date modified, int count, byte[] data) {
		this(tableId, id, created, modified, count, (byte) 0x00, data);
	}

	public Event(int tableId, long id, Date created, Date modified, int count, byte reserved, byte[] data) {
		this.tableId = tableId;
		this.id = id;
		this.created = created;
		this.modified = modified;
		this.count = count;
		this.reserved = reserved;
		this.data = data;
	}

	public int getTableId() {
		return tableId;
	}

	public long getId() {
		return id;
	}

	public Date getCreated() {
		return created;
	}

	public Date getModified() {
		return modified;
	}

	public int getCount() {
		return count;
	}

	public byte getReserved() {
		return reserved;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public String toString() {
		return "Event [tableId=" + tableId + ", id=" + id + ", created=" + created + ", modified=" + modified + ", count="
				+ count + "]";
	}
}
