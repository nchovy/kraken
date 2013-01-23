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
import java.util.Map;

import org.krakenapps.confdb.CollectionName;

/**
 * @since 0.9
 * @author xeraph
 */
@CollectionName("index")
public class LogIndexSchema {
	// index id (global unique)
	private int id;

	// associated table name
	private String tableName;

	// unique per each log table (not global unique)
	private String indexName;

	// need to build old index when create this index?
	private boolean buildPastIndex;

	// min indexing date, inclusive (yyyy-MM-dd only)
	private Date minIndexDay;

	// index tokenizer name (profile has tokenizer configuration)
	private String tokenizerName;

	private Map<String, String> tokenizerConfigs;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		LogIndexSchema other = (LogIndexSchema) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

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

	public Date getMinIndexDay() {
		return minIndexDay;
	}

	public void setMinIndexDay(Date minIndexDay) {
		this.minIndexDay = minIndexDay;
	}

	public boolean isBuildPastIndex() {
		return buildPastIndex;
	}

	public void setBuildPastIndex(boolean buildPastIndex) {
		this.buildPastIndex = buildPastIndex;
	}

	public String getTokenizerName() {
		return tokenizerName;
	}

	public void setTokenizerName(String tokenizerName) {
		this.tokenizerName = tokenizerName;
	}

	public Map<String, String> getTokenizerConfigs() {
		return tokenizerConfigs;
	}

	public void setTokenizerConfigs(Map<String, String> tokenizerConfigs) {
		this.tokenizerConfigs = tokenizerConfigs;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		String min = "unbound";
		if (minIndexDay != null)
			min = "from " + dateFormat.format(minIndexDay);

		return "id=" + id + ", table=" + tableName + ", index=" + indexName + ", period (" + min + "), tokenizer="
				+ tokenizerName;
	}

}
