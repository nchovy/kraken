package org.krakenapps.logdb.arbiter.impl;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.logdb.arbiter.ArbiterService;
import org.krakenapps.logdb.arbiter.ArbiterQueryStatus;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcContext;
import org.krakenapps.rpc.RpcMethod;
import org.krakenapps.rpc.RpcSession;
import org.krakenapps.rpc.RpcSessionEvent;
import org.krakenapps.rpc.SimpleRpcService;

@Component(name = "logdb-arbiter")
@Provides
public class ArbiterRpcService extends SimpleRpcService implements ArbiterService {

	public void connect(Map<String, Object> params) {
		String guid = (String) params.get("guid");
		RpcSession session = RpcContext.getSession();
		RpcConnection conn = session.getConnection();
		session.setProperty("guid", guid);
		connect(guid, conn.getRemoteAddress());
	}

	public void disconnect(String guid) {
		disconnect(guid);
	}

	@RpcMethod(name = "onDataSourceEvent")
	public void onDataSourceEvent(String name, String action, Map<String, Object> state) {
		RpcSession session = RpcContext.getSession();
		String guid = (String) session.getProperty("guid");

		if (action.equals("add") || action.equals("update"))
			onUpsertDataSource(guid, name, state);
		else if (action.equals("remove"))
			onRemoveDataSource(guid, name);
	}

	@RpcMethod(name = "onQueryEvent")
	public void onQueryEvent(int queryId, String action, Map<String, Object> state) {
		RpcSession session = RpcContext.getSession();
		String guid = (String) session.getProperty("guid");

		if (action.equals("create"))
			onCreateQuery(guid, queryId, (String) state.get("query"));
		else if (action.equals("remove"))
			onRemoveQuery(guid, queryId);
		else if (action.equals("start"))
			onStartQuery(guid, queryId);
		else if (action.equals("eof"))
			onEofQuery(guid, queryId);
	}

	@Override
	public void sessionClosed(RpcSessionEvent e) {
		String guid = (String) e.getSession().getProperty("guid");
		if (guid != null)
			disconnect(guid);
	}

	@Override
	public List<ArbiterQueryStatus> getQueries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArbiterQueryStatus createQuery(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startQuery(String guid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeQuery(String query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connect(String guid, InetSocketAddress remoteAddress) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpsertDataSource(String guid, String name, Map<String, Object> state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRemoveDataSource(String guid, String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCreateQuery(String guid, int queryId, String string) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRemoveQuery(String guid, int queryId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartQuery(String guid, int queryId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEofQuery(String guid, int queryId) {
		// TODO Auto-generated method stub

	}

}
