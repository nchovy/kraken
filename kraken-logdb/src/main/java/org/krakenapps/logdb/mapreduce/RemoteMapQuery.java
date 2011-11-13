package org.krakenapps.logdb.mapreduce;

public class RemoteMapQuery {
	/**
	 * mapreduce query guid
	 */
	private String guid;

	/**
	 * downstream peer guid
	 */
	private String peerGuid;

	/**
	 * remote peer's query id
	 */
	private int queryId;

	public RemoteMapQuery(String guid, String peerGuid, int queryId) {
		this.guid = guid;
		this.peerGuid = peerGuid;
		this.queryId = queryId;
	}

	public String getGuid() {
		return guid;
	}

	public String getPeerGuid() {
		return peerGuid;
	}

	public int getQueryId() {
		return queryId;
	}
}
