package org.krakenapps.logdb.mapreduce;

import java.util.Collection;
import java.util.List;

import org.krakenapps.logdb.query.command.RpcFrom;
import org.krakenapps.logdb.query.command.RpcTo;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcConnectionProperties;

public interface MapReduceService {
	RpcFrom getRpcFrom(String guid);

	RpcTo getRpcTo(String guid);

	List<MapReduceQueryStatus> getQueries();

	MapReduceQueryStatus createQuery(String query);

	void startQuery(String guid);

	void removeQuery(String guid);

	List<RemoteQuery> getRemoteQueries();

	Collection<RpcConnection> getUpstreamConnections();

	Collection<RpcConnection> getDownstreamConnections();

	RpcConnection connect(RpcConnectionProperties props);

	void disconnect(String guid);
}
