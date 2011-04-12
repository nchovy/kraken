/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.logstorage.engine;

import java.util.Date;

public class TabletKey implements Comparable<TabletKey> {
	private int tableId;
	private Date day;

	public TabletKey(int tableId, Date day) {
		this.tableId = tableId;
		this.day = day;
	}

	public int getTableId() {
		return tableId;
	}

	public Date getDay() {
		return day;
	}

	public static byte[] encode(TabletKey key) {
		byte[] b = new byte[4];
		b[0] = (byte) ((key.tableId >> 8) & 0xff);
		b[1] = (byte) (key.tableId & 0xff);

		int days = (int) (key.day.getTime() / 86400000) + 1;
		b[2] = (byte) ((days >> 8) & 0xff);
		b[3] = (byte) (days & 0xff);
		return b;
	}

	public static TabletKey decode(byte[] b) {
		int tableId = (((b[0] << 8) & 0xff00) | (b[1] & 0xff));
		int days = (((b[2] << 8) & 0xff00) | (b[3] & 0xff));
		Date d = new Date(days * 86400000l);
		return new TabletKey(tableId, DateUtil.getDay(d));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		TabletKey other = (TabletKey) obj;
		if (day == null) {
			if (other.day != null)
				return false;
		} else if (!day.equals(other.day))
			return false;
		if (tableId != other.tableId)
			return false;
		return true;
	}

	/**
	 * default to descending order
	 */
	@Override
	public int compareTo(TabletKey o) {
		if (tableId != o.tableId)
			return o.tableId - tableId;

		return (int) (o.day.getTime() - day.getTime());
	}

	@Override
	public String toString() {
		return String.format("table id=%d, day=%s", tableId, DateUtil.getDayText(day));
	}

}