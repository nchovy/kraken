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
public class LogIndexingTask {
	private int indexId;
	private String indexName;
	private String tableName;
	private Date minDay;
	private Date maxDay;

	private FileSet diskFileSet;
	private FileSet onlineFileSet;
	private long diskMinId;
	private long diskMaxId;
	private long onlineMinId;
	private long onlineMaxId;

	public int getIndexId() {
		return indexId;
	}

	public void setIndexId(int indexId) {
		this.indexId = indexId;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Date getMinDay() {
		return minDay;
	}

	public void setMinDay(Date minDay) {
		this.minDay = minDay;
	}

	public Date getMaxDay() {
		return maxDay;
	}

	public void setMaxDay(Date maxDay) {
		this.maxDay = maxDay;
	}

	public FileSet getOnlineFileSet() {
		return onlineFileSet;
	}

	public void setOnlineFileSet(FileSet onlineFileSet) {
		this.onlineFileSet = onlineFileSet;
	}

	public long getDiskMinId() {
		return diskMinId;
	}

	public void setDiskMinId(long diskMinId) {
		this.diskMinId = diskMinId;
	}

	public long getDiskMaxId() {
		return diskMaxId;
	}

	public void setDiskMaxId(long diskMaxId) {
		this.diskMaxId = diskMaxId;
	}

	public long getOnlineMinId() {
		return onlineMinId;
	}

	public void setOnlineMinId(long onlineMinId) {
		this.onlineMinId = onlineMinId;
	}

	public long getOnlineMaxId() {
		return onlineMaxId;
	}

	public void setOnlineMaxId(long onlineMaxId) {
		this.onlineMaxId = onlineMaxId;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String min = "unbound";
		String max = "unbound";

		if (minDay != null)
			min = dateFormat.format(minDay);
		if (maxDay != null)
			max = dateFormat.format(maxDay);

		return String.format("indexing task for table %s, duration [%s~%s]", tableName, min, max);
	}

}
