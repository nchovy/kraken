package org.krakenapps.logdb.mapreduce;

import java.util.List;

public class MapReduceQueryStatus {
	private String guid;
	private String owner;
	private String query;
	private List<MapReduceQuery> mapperQueries;
	private MapReduceQuery reducerQuery;

	public MapReduceQueryStatus(String guid, String owner, String query) {
		this.guid = guid;
		this.owner = owner;
		this.query = query;
	}

	public String getGuid() {
		return guid;
	}

	public String getOwner() {
		return owner;
	}

	public String getQuery() {
		return query;
	}

	public List<MapReduceQuery> getMapperQueries() {
		return mapperQueries;
	}

	public void setMapperQueries(List<MapReduceQuery> mapperQueries) {
		this.mapperQueries = mapperQueries;
	}

	public MapReduceQuery getReducerQuery() {
		return reducerQuery;
	}

	public void setReducerQuery(MapReduceQuery reducerQuery) {
		this.reducerQuery = reducerQuery;
	}

	@Override
	public String toString() {
		return "arbiter query status: guid=" + guid + ", owner=" + owner + "query=" + query;
	}

}
