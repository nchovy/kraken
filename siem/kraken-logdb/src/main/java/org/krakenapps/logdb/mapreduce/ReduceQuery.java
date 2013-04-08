package org.krakenapps.logdb.mapreduce;

import org.krakenapps.logdb.LogQuery;

public class ReduceQuery {
	/**
	 * mapreduce query guid
	 */
	private String guid;

	/**
	 * local reduce query
	 */
	private LogQuery query;

	public ReduceQuery(String guid, LogQuery query) {
		this.guid = guid;
		this.query = query;
	}

	public String getGuid() {
		return guid;
	}

	public LogQuery getQuery() {
		return query;
	}
}
