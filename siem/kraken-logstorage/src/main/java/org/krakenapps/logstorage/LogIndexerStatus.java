/*
 * Copyright 2013 Future Systems
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

/**
 * @since 0.9
 * @author xeraph
 */
public class LogIndexerStatus {
	private String tableName;
	private String indexName;
	private Date day;
	private Date lastFlush;
	private long queueCount;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public Date getDay() {
		return day;
	}

	public void setDay(Date day) {
		this.day = day;
	}

	public Date getLastFlush() {
		return lastFlush;
	}

	public void setLastFlush(Date lastFlush) {
		this.lastFlush = lastFlush;
	}

	public long getQueueCount() {
		return queueCount;
	}

	public void setQueueCount(long queueCount) {
		this.queueCount = queueCount;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return "table=" + tableName + ", index=" + indexName + " (" + dateFormat.format(day) + "), buffered=" + queueCount;
	}

}
