package org.krakenapps.logdb;

import java.util.Map;

public interface DataSource {
	/**
	 * @return "local" for local node, guid for remote node
	 */
	String getNodeGuid();

	/**
	 * @return "table" or "rpc"
	 */
	String getType();

	/**
	 * @return the data source name
	 */
	String getName();

	/**
	 * @return the data source metadata
	 */
	Map<String, Object> getMetadata();

}
