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
import java.util.HashMap;
import java.util.Map;

/**
 * @since 0.9
 * @author xeraph
 */
public class BatchIndexingTask {
	private String tableName;
	private String indexName;
	private int tableId;
	private int indexId;
	private Date minDay;
	private Date maxDay;
	private boolean canceled;
	private Date since = new Date();

	private Map<Date, BatchIndexingStatus> builds = new HashMap<Date, BatchIndexingStatus>();

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

	public int getTableId() {
		return tableId;
	}

	public void setTableId(int tableId) {
		this.tableId = tableId;
	}

	public int getIndexId() {
		return indexId;
	}

	public void setIndexId(int indexId) {
		this.indexId = indexId;
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

	public Map<Date, BatchIndexingStatus> getBuilds() {
		return builds;
	}

	public void setBuilds(Map<Date, BatchIndexingStatus> builds) {
		this.builds = builds;
	}

	public boolean isDone() {
		if (builds == null)
			return true;

		for (BatchIndexingStatus s : builds.values()) {
			if (!s.isDone())
				return false;
		}

		return true;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public Date getSince() {
		return since;
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
