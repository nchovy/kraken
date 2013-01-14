package org.krakenapps.logstorage;

import java.util.Date;

public class LogIndexQuery {
	private String tableName;
	private String indexName;
	private String term;
	private Date minDay;
	private Date maxDay;

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

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
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
}
