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
package org.krakenapps.logstorage.file;

import java.nio.ByteBuffer;
import java.util.Date;

public class LogRecord implements Comparable<LogRecord> {
	private Date date;
	private long id;
	private ByteBuffer data;

	public LogRecord(Date date, ByteBuffer data) {
		this(date, 0, data);
	}

	public LogRecord(Date date, long id, ByteBuffer data) {
		this.date = date;
		this.id = id;
		this.data = data;
	}

	public Date getDate() {
		return date;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ByteBuffer getData() {
		return data;
	}

	@Override
	public int compareTo(LogRecord o) {
		return (int) (id - o.id);
	}

}
