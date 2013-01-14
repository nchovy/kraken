package org.krakenapps.logstorage;

import java.util.Date;

public class LogIndexConfig {
	private int id;
	private String tableName;
	private String indexName;
	private String type;
	private boolean buildPastIndex;
	private Date minIndexDay;
	private Date maxIndexDay;
	private String tokenizerProfile;

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getMinIndexDay() {
		return minIndexDay;
	}

	public void setMinIndexDay(Date minIndexDay) {
		this.minIndexDay = minIndexDay;
	}

	public Date getMaxIndexDay() {
		return maxIndexDay;
	}

	public void setMaxIndexDay(Date maxIndexDay) {
		this.maxIndexDay = maxIndexDay;
	}

	public boolean isBuildPastIndex() {
		return buildPastIndex;
	}

	public void setBuildPastIndex(boolean buildPastIndex) {
		this.buildPastIndex = buildPastIndex;
	}

	public String getTokenizerProfile() {
		return tokenizerProfile;
	}

	public void setTokenizerProfile(String tokenizerProfile) {
		this.tokenizerProfile = tokenizerProfile;
	}
}
