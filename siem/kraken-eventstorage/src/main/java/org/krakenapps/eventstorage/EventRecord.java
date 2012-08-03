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
