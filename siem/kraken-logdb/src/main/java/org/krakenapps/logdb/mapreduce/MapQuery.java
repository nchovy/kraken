package org.krakenapps.logdb.mapreduce;

import org.krakenapps.logdb.LogQuery;
import org.krakenapps.rpc.RpcConnection;

public class MapQuery {
	/**
	 * mapreduce query guid
	 */
	private String guid;

	/**
	 * upstream rpc connection
	 */
	private RpcConnection connection;

	/**
	 * local node's query
	 */
	private LogQuery query;

	public MapQuery(String guid, RpcConnection connection) {
		this.guid = guid;
		this.connection = connection;
	}

	public String getGuid() {
		return guid;
	}

	public RpcConnection getConnection() {
		return connection;
	}

	public LogQuery getQuery() {
		return query;
	}

	public void setQuery(LogQuery query) {
		this.query = query;
	}

}
