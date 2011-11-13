package org.krakenapps.logdb.impl;

import static org.krakenapps.bnf.Syntax.k;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.DataSource;
import org.krakenapps.logdb.DataSourceEventListener;
import org.krakenapps.logdb.DataSourceRegistry;
import org.krakenapps.logdb.SyntaxProvider;
import org.krakenapps.logdb.arbiter.ArbiterService;
import org.krakenapps.logdb.arbiter.ArbiterQueryStatus;
import org.krakenapps.logdb.query.StringPlaceholder;
import org.krakenapps.logdb.query.command.Rpc;
import org.krakenapps.logdb.query.parser.QueryParser;
import org.krakenapps.rpc.RpcAgent;
import org.krakenapps.rpc.RpcClient;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcConnectionProperties;
import org.krakenapps.rpc.RpcContext;
import org.krakenapps.rpc.RpcMethod;
import org.krakenapps.rpc.RpcSession;
import org.krakenapps.rpc.RpcSessionEvent;
import org.krakenapps.rpc.SimpleRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "logdb-arbiter")
@Provides
public class ArbiterRpcService extends SimpleRpcService implements ArbiterService, DataSourceEventListener {
	private final Logger logger = LoggerFactory.getLogger(ArbiterRpcService.class.getName());
	private ConcurrentMap<String, ArbiterQueryStatus> queries;

	@Requires
	private RpcAgent agent;

	@Requires
	private SyntaxProvider syntaxProvider;

	@Requires
	private DataSourceRegistry dataSourceRegistry;

	@SuppressWarnings("unused")
	@ServiceProperty(name = "rpc.name", value = "logdb-arbiter")
	private String name;

	private ConcurrentMap<String, Rpc> rpcFromMap;
	private ConcurrentMap<String, Rpc> rpcToMap;
	private RpcFromParser rpcFromParser;
	private RpcToParser rpcToParser;

	private ConcurrentMap<String, RpcConnection> upstreams;

	public ArbiterRpcService() {
	}

	@Validate
	public void start() {
		queries = new ConcurrentHashMap<String, ArbiterQueryStatus>();
		rpcFromMap = new ConcurrentHashMap<String, Rpc>();
		rpcToMap = new ConcurrentHashMap<String, Rpc>();
		upstreams = new ConcurrentHashMap<String, RpcConnection>();

		rpcFromParser = new RpcFromParser();
		syntaxProvider.addParsers(Arrays.asList(rpcFromParser, rpcToParser));
		dataSourceRegistry.addListener(this);
	}

	@Invalidate
	public void stop() {
		if (syntaxProvider != null)
			syntaxProvider.removeParsers(Arrays.asList(rpcFromParser, rpcToParser));

		if (dataSourceRegistry != null)
			dataSourceRegistry.removeListener(this);
	}

	private class RpcFromParser implements QueryParser {
		@Override
		public void addSyntax(Syntax syntax) {
			syntax.add("rpcfrom", this, k("rpcfrom"), new StringPlaceholder());
			syntax.addRoot("rpcfrom");
		}

		@Override
		public Object parse(Binding b) {
			String guid = (String) b.getChildren()[1].getValue();
			Rpc rpc = new Rpc(guid, false);
			rpcFromMap.put(guid, rpc);
			return rpc;
		}
	}

	private class RpcToParser implements QueryParser {
		@Override
		public void addSyntax(Syntax syntax) {
			syntax.add("rpcto", this, k("rpcto"), new StringPlaceholder());
			syntax.addRoot("rpcto");
		}

		@Override
		public Object parse(Binding b) {
			String guid = (String) b.getChildren()[1].getValue();
			Rpc rpc = new Rpc(guid, true);
			rpcToMap.put(guid, rpc);
			return rpc;
		}
	}

	@Override
	public Rpc getRpcFrom(String guid) {
		return rpcFromMap.get(guid);
	}

	@Override
	public Rpc getRpcTo(String guid) {
		return rpcToMap.get(guid);
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
		return new ArrayList<ArbiterQueryStatus>(queries.values());
	}

	@Override
	public ArbiterQueryStatus createQuery(String query) {
		return null;
	}

	@Override
	public void startQuery(String guid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeQuery(String guid) {
		rpcFromMap.remove(guid);
		rpcToMap.remove(guid);
	}

	@Override
	public Collection<RpcConnection> getUpstreamConnections() {
		return Collections.unmodifiableCollection(upstreams.values());
	}

	@Override
	public RpcConnection connect(RpcConnectionProperties props) {
		try {
			RpcClient client = new RpcClient(agent.getGuid());
			RpcConnection conn = client.connect(props);

			if (conn != null) {
				// wait until peering completed
				int i = 0;
				while (conn.getPeerGuid() == null) {
					if (i > 50)
						break;

					Thread.sleep(100);
					i++;
				}

				upstreams.put(conn.getPeerGuid(), conn);

				for (DataSource ds : dataSourceRegistry.getAll())
					if (!ds.getType().equals("rpc"))
						notifyDataSourceUpdate(ds, conn);

				return conn;
			}

			return null;
		} catch (InterruptedException e) {
			throw new IllegalStateException("connection timeout");
		}
	}

	@Override
	public void disconnect(String guid) {
		RpcConnection conn = upstreams.remove(guid);
		if (conn != null && conn.isOpen())
			conn.close();
	}

	@RpcMethod(name = "onUpsertDataSource")
	public void onUpsertDataSource(String name, Map<String, Object> metadata) {
		String guid = RpcContext.getConnection().getPeerGuid();
		onUpsertDataSource(guid, name, metadata);
	}

	@Override
	public void onUpsertDataSource(String guid, String name, Map<String, Object> metadata) {
		logger.info("kraken logdb: on update data source [guid={}, name={}]", guid, name);
		dataSourceRegistry.update(new RpcDataSource(guid, name, metadata));
	}

	@RpcMethod(name = "onRemoveDataSource")
	public void onRemoveDataSource(String name) {
		String guid = RpcContext.getConnection().getPeerGuid();
		onRemoveDataSource(guid, name);
	}

	@Override
	public void onRemoveDataSource(String guid, String name) {
		logger.info("kraken logdb: on remove data source [guid={}, name={}]", guid, name);
		dataSourceRegistry.remove(new RpcDataSource(guid, name));
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

	//
	// DataSourceEventListener callbacks
	//

	@Override
	public void onUpdate(DataSource ds) {
		for (RpcConnection conn : upstreams.values()) {
			notifyDataSourceUpdate(ds, conn);
		}
	}

	@Override
	public void onRemove(DataSource ds) {
		for (RpcConnection conn : upstreams.values()) {
			RpcSession session = null;
			try {
				session = conn.createSession("logdb-arbiter");
				session.post("onRemoveDataSource", ds.getName());
			} catch (Exception e) {
				logger.warn("kraken logdb: cannot remove datasource info", e);
			} finally {
				if (session != null)
					session.close();
			}
		}
	}

	private void notifyDataSourceUpdate(DataSource ds, RpcConnection conn) {
		RpcSession session = null;
		try {
			session = conn.createSession("logdb-arbiter");
			session.post("onUpsertDataSource", ds.getName(), ds.getMetadata());
			logger.info("kraken logdb: notified data source [type={}, name={}] update ", ds.getType(), ds.getName());
		} catch (Exception e) {
			logger.warn("kraken logdb: cannot update datasource info", e);
		} finally {
			if (session != null)
				session.close();
		}
	}

}
