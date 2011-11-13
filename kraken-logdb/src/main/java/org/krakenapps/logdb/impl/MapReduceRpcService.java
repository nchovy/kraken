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
import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryEventListener;
import org.krakenapps.logdb.LogQueryService;
import org.krakenapps.logdb.LogQueryStatus;
import org.krakenapps.logdb.SyntaxProvider;
import org.krakenapps.logdb.mapreduce.MapReduceQueryStatus;
import org.krakenapps.logdb.mapreduce.MapReduceService;
import org.krakenapps.logdb.mapreduce.RemoteQuery;
import org.krakenapps.logdb.mapreduce.RemoteQueryKey;
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

@Component(name = "logdb-mapreduce")
@Provides
public class MapReduceRpcService extends SimpleRpcService implements MapReduceService, LogQueryEventListener,
		DataSourceEventListener {
	private final Logger logger = LoggerFactory.getLogger(MapReduceRpcService.class.getName());
	private ConcurrentMap<String, MapReduceQueryStatus> queries;

	@Requires
	private RpcAgent agent;

	@Requires
	private SyntaxProvider syntaxProvider;

	@Requires
	private DataSourceRegistry dataSourceRegistry;

	@Requires
	private LogQueryService queryService;

	@SuppressWarnings("unused")
	@ServiceProperty(name = "rpc.name", value = "logdb-mapreduce")
	private String name;

	private ConcurrentMap<String, Rpc> rpcFromMap;
	private ConcurrentMap<String, Rpc> rpcToMap;
	private RpcFromParser rpcFromParser;
	private RpcToParser rpcToParser;

	private ConcurrentMap<RemoteQueryKey, RemoteQuery> remoteQueries;
	private ConcurrentMap<String, RpcConnection> upstreams;
	private ConcurrentMap<String, RpcConnection> downstreams;

	public MapReduceRpcService() {
	}

	@Validate
	public void start() {
		queries = new ConcurrentHashMap<String, MapReduceQueryStatus>();
		rpcFromMap = new ConcurrentHashMap<String, Rpc>();
		rpcToMap = new ConcurrentHashMap<String, Rpc>();
		upstreams = new ConcurrentHashMap<String, RpcConnection>();
		downstreams = new ConcurrentHashMap<String, RpcConnection>();
		remoteQueries = new ConcurrentHashMap<RemoteQueryKey, RemoteQuery>();

		rpcFromParser = new RpcFromParser();
		syntaxProvider.addParsers(Arrays.asList(rpcFromParser, rpcToParser));
		dataSourceRegistry.addListener(this);
		queryService.addListener(this);
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

	@RpcMethod(name = "onDataSourceChange")
	public void onDataSourceChange(String name, String action, Map<String, Object> metadata) {
		RpcSession session = RpcContext.getSession();
		String guid = session.getConnection().getPeerGuid();

		if (action.equals("add") || action.equals("update")) {
			logger.info("kraken logdb: on update data source [guid={}, name={}]", guid, name);
			dataSourceRegistry.update(new RpcDataSource(guid, name, metadata));
		} else if (action.equals("remove")) {
			logger.info("kraken logdb: on remove data source [guid={}, name={}]", guid, name);
			dataSourceRegistry.remove(new RpcDataSource(guid, name));
		}
	}

	@RpcMethod(name = "onQueryStatusChange")
	public void onQueryStatusChange(int queryId, String action, String queryString) {
		RpcSession session = RpcContext.getSession();
		String guid = session.getConnection().getPeerGuid();
		RemoteQueryKey key = new RemoteQueryKey(guid, queryId);
		LogQueryStatus status = LogQueryStatus.valueOf(action);

		switch (status) {
		case Created:
			remoteQueries.put(key, new RemoteQuery(guid, queryId, queryString));
			break;
		case Removed:
			remoteQueries.remove(key);
			break;
		case Started: {
			RemoteQuery q = remoteQueries.get(key);
			if (q != null)
				q.setRunning(true);
		}
			break;
		case Stopped: {
			RemoteQuery q = remoteQueries.get(key);
			if (q != null)
				q.setRunning(false);
		}
			break;
		case Eof: {
			RemoteQuery q = remoteQueries.get(key);
			if (q != null)
				q.setEnd(true);
		}
			break;
		}
	}

	@Override
	public void sessionClosed(RpcSessionEvent e) {
		String guid = (String) e.getSession().getProperty("guid");
		if (guid != null)
			disconnect(guid);
	}

	@Override
	public List<MapReduceQueryStatus> getQueries() {
		return new ArrayList<MapReduceQueryStatus>(queries.values());
	}

	@Override
	public MapReduceQueryStatus createQuery(String query) {
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
	public List<RemoteQuery> getRemoteQueries() {
		return new ArrayList<RemoteQuery>(remoteQueries.values());
	}

	@Override
	public Collection<RpcConnection> getUpstreamConnections() {
		return Collections.unmodifiableCollection(upstreams.values());
	}

	@Override
	public Collection<RpcConnection> getDownstreamConnections() {
		return Collections.unmodifiableCollection(downstreams.values());
	}

	/**
	 * register downstream connection when any logdb session opened
	 */
	@Override
	public void sessionOpened(RpcSessionEvent e) {
		RpcConnection conn = e.getSession().getConnection();
		if (!downstreams.containsKey(conn.getPeerGuid())) {
			downstreams.put(conn.getPeerGuid(), conn);
			logger.info("kraken logdb: downstream connection [{}] opened", conn);
		}
	}

	@Override
	public void connectionClosed(RpcConnection conn) {
		downstreams.remove(conn.getPeerGuid());
		logger.info("kraken logdb: downstream connection [{}] closed", conn);
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
						notifyDataSourceChange(ds, "update", conn);

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

	//
	// DataSourceEventListener callbacks
	//

	@Override
	public void onUpdate(DataSource ds) {
		for (RpcConnection conn : upstreams.values()) {
			notifyDataSourceChange(ds, "update", conn);
		}
	}

	@Override
	public void onRemove(DataSource ds) {
		for (RpcConnection conn : upstreams.values()) {
			notifyDataSourceChange(ds, "remove", conn);
		}
	}

	private void notifyDataSourceChange(DataSource ds, String action, RpcConnection conn) {
		RpcSession session = null;
		try {
			session = conn.createSession("logdb-mapreduce");
			session.post("onDataSourceChange", ds.getName(), action, ds.getMetadata());
			logger.info("kraken logdb: notified data source [type={}, name={}, action={}] ",
					new Object[] { ds.getType(), ds.getName(), action });
		} catch (Exception e) {
			logger.warn("kraken logdb: cannot update datasource info", e);
		} finally {
			if (session != null)
				session.close();
		}
	}

	//
	// LogQueryEventListener
	//

	@Override
	public void onQueryStatusChange(LogQuery query, LogQueryStatus status) {
		notifyQueryStatus(query.getId(), status, query.getQueryString());
	}

	private void notifyQueryStatus(int id, LogQueryStatus status, String queryString) {
		for (RpcConnection conn : upstreams.values()) {
			RpcSession session = null;
			try {
				session = conn.createSession("logdb-mapreduce");
				session.post("onQueryStatusChange", id, status.name(), queryString);
				logger.info("kraken logdb: notified query status [id={}, status={}] to peer [{}]", new Object[] { id,
						status, conn.getPeerGuid() });
			} catch (Exception e) {
				logger.warn("kraken logdb: cannot update datasource info", e);
			} finally {
				if (session != null)
					session.close();
			}
		}
	}

}
