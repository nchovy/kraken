package org.krakenapps.logdb.arbiter;

import java.util.List;

public class ArbiterQueryStatus {
	private String guid;
	private String owner;
	private String query;
	private List<DistributedQuery> mapperQueries;
	private DistributedQuery reducerQuery;

	public ArbiterQueryStatus(String guid, String owner, String query) {
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

	public List<DistributedQuery> getMapperQueries() {
		return mapperQueries;
	}

	public void setMapperQueries(List<DistributedQuery> mapperQueries) {
		this.mapperQueries = mapperQueries;
	}

	public DistributedQuery getReducerQuery() {
		return reducerQuery;
	}

	public void setReducerQuery(DistributedQuery reducerQuery) {
		this.reducerQuery = reducerQuery;
	}

	@Override
	public String toString() {
		return "arbiter query status: guid=" + guid + ", owner=" + owner + "query=" + query;
	}

}
