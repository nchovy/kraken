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
package org.krakenapps.logstorage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogKey implements Comparable<LogKey> {
	private int tableId;
	private Date day;
	private int logId;

	public LogKey(int tableId, Date day, int logId) {
		this.tableId = tableId;
		this.day = day;
		this.logId = logId;
	}

	public int getTableId() {
		return tableId;
	}

	public Date getDay() {
		return day;
	}

	public int getLogId() {
		return logId;
	}

	@Override
	public int compareTo(LogKey o) {
		if (tableId != o.tableId)
			return tableId - o.tableId;

		if (!day.equals(o.day))
			return (int) (day.getTime() - o.day.getTime());

		return logId - o.logId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((day == null) ? 0 : day.hashCode());
		result = prime * result + logId;
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
		LogKey other = (LogKey) obj;
		if (day == null) {
			if (other.day != null)
				return false;
		} else if (!day.equals(other.day))
			return false;
		if (logId != other.logId)
			return false;
		if (tableId != other.tableId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return String.format("table=%s, day=%s, log=%s", tableId, dateFormat.format(day), logId);
	}

}
