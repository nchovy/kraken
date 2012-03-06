package org.krakenapps.logdb.mapreduce;

import java.util.List;

public class MapReduceQueryStatus {
	private String guid;
	private String query;
	private List<RemoteMapQuery> mapQueries;
	private ReduceQuery reduceQuery;

	public MapReduceQueryStatus(String guid, String query, List<RemoteMapQuery> mapQueries, ReduceQuery reduceQuery) {
		this.guid = guid;
		this.query = query;
		this.mapQueries = mapQueries;
		this.reduceQuery = reduceQuery;
	}

	public String getGuid() {
		return guid;
	}

	public String getQuery() {
		return query;
	}

	public List<RemoteMapQuery> getMapQueries() {
		return mapQueries;
	}

	public void setMapQueries(List<RemoteMapQuery> mapQueries) {
		this.mapQueries = mapQueries;
	}

	public ReduceQuery getReduceQuery() {
		return reduceQuery;
	}

	public void setReduceQuery(ReduceQuery reduceQuery) {
		this.reduceQuery = reduceQuery;
	}

	@Override
	public String toString() {
		return "guid=" + guid + ", query=" + query;
	}

}
