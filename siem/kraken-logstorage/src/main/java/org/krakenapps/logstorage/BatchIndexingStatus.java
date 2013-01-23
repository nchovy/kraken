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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.krakenapps.logstorage.index.InvertedIndexFileSet;

/**
 * @since 0.9
 * @author xeraph
 */
public class BatchIndexingStatus implements Comparable<BatchIndexingStatus> {
	private BatchIndexingTask task;

	private Date day;

	private InvertedIndexFileSet files;

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
		return files.getIndexFile();
	}

	public File getDataFile() {
		return files.getDataFile();
	}

	public InvertedIndexFileSet getFiles() {
		return files;
	}

	public void setFiles(InvertedIndexFileSet files) {
		this.files = files;
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

	@Override
	public int compareTo(BatchIndexingStatus o) {
		if (o == null)
			return 1;

		return (int) (day.getTime() - o.day.getTime());
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return String.format("table=%s, index=%s, day=%s", task.getTableName(), task.getIndexName(), dateFormat.format(day));
	}
}
