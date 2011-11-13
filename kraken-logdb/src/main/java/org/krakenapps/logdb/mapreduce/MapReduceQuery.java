package org.krakenapps.logdb.mapreduce;

public class MapReduceQuery {
	private String guid;
	private int queryId;
	private String query;

	public MapReduceQuery(String nodeGuid, int queryId, String query) {
		this.guid = nodeGuid;
		this.queryId = queryId;
		this.query = query;
	}

	public String getNodeGuid() {
		return guid;
	}

	public int getQueryId() {
		return queryId;
	}

	public String getQuery() {
		return query;
	}

	@Override
	public String toString() {
		return "node=" + guid + ", query=" + queryId + ":" + query;
	}
}
