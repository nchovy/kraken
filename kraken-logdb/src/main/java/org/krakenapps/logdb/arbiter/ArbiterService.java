package org.krakenapps.logdb.arbiter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.krakenapps.logdb.query.command.Rpc;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcConnectionProperties;

public interface ArbiterService {
	Rpc getRpcFrom(String guid);

	Rpc getRpcTo(String guid);

	List<ArbiterQueryStatus> getQueries();

	ArbiterQueryStatus createQuery(String query);

	void startQuery(String guid);

	void removeQuery(String guid);

	Collection<RpcConnection> getUpstreamConnections();

	RpcConnection connect(RpcConnectionProperties props);

	void disconnect(String guid);

	void onUpsertDataSource(String guid, String name, Map<String, Object> state);

	void onRemoveDataSource(String guid, String name);

	void onCreateQuery(String guid, int queryId, String string);

	void onRemoveQuery(String guid, int queryId);

	void onStartQuery(String guid, int queryId);

	void onEofQuery(String guid, int queryId);
}
