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

import java.io.File;
import java.util.Date;

/**
 * @since 0.9
 * @author xeraph
 */
public class BatchIndexingStatus {
	private BatchIndexingTask task;

	private Date day;

	// position offsets
	private File indexFile;

	// term and posting data blocks
	private File dataFile;

	private long tokenCount;
	private long logCount;
	private boolean done;

	public BatchIndexingTask getTask() {
		return task;
	}

	public void setTask(BatchIndexingTask task) {
		this.task = task;
	}

	public Date getDay() {
		return day;
	}

	public void setDay(Date day) {
		this.day = day;
	}

	public File getIndexFile() {
		return indexFile;
	}

	public void setIndexFile(File indexFile) {
		this.indexFile = indexFile;
	}

	public File getDataFile() {
		return dataFile;
	}

	public void setDataFile(File dataFile) {
		this.dataFile = dataFile;
	}

	public long getTokenCount() {
		return tokenCount;
	}

	public void setTokenCount(long tokenCount) {
		this.tokenCount = tokenCount;
	}

	public void addTokenCount(long value) {
		this.tokenCount += value;
	}

	public long getLogCount() {
		return logCount;
	}

	public void setLogCount(long logCount) {
		this.logCount = logCount;
	}

	public void addLogCount(long value) {
		this.logCount += value;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}
}
