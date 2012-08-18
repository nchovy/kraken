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
