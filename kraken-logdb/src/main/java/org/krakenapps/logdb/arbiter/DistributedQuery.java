package org.krakenapps.logdb.arbiter;

public class DistributedQuery {
	private String guid;
	private int queryId;
	private String query;

	public DistributedQuery(String nodeGuid, int queryId, String query) {
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
