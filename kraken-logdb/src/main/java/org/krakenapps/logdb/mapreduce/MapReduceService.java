package org.krakenapps.logdb.mapreduce;

import java.util.Collection;
import java.util.List;

import org.krakenapps.logdb.query.command.Rpc;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcConnectionProperties;

public interface MapReduceService {
	Rpc getRpcFrom(String guid);

	Rpc getRpcTo(String guid);

	List<MapReduceQueryStatus> getQueries();

	MapReduceQueryStatus createQuery(String query);

	void startQuery(String guid);

	void removeQuery(String guid);

	List<RemoteQuery> getRemoteQueries();

	Collection<RpcConnection> getUpstreamConnections();

	RpcConnection connect(RpcConnectionProperties props);

	void disconnect(String guid);
}
