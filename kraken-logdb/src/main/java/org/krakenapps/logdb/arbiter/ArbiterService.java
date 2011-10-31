package org.krakenapps.logdb.arbiter;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public interface ArbiterService {
	List<ArbiterQueryStatus> getQueries();

	ArbiterQueryStatus createQuery(String query);
	
	void startQuery(String guid);

	void removeQuery(String query);

	void connect(String guid, InetSocketAddress remoteAddress);

	void disconnect(String guid);

	void onUpsertDataSource(String guid, String name, Map<String, Object> state);

	void onRemoveDataSource(String guid, String name);

	void onCreateQuery(String guid, int queryId, String string);

	void onRemoveQuery(String guid, int queryId);

	void onStartQuery(String guid, int queryId);

	void onEofQuery(String guid, int queryId);
}
