package org.krakenapps.logdb.impl;

import static org.krakenapps.bnf.Syntax.k;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.api.Primitive;
import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.DataSource;
import org.krakenapps.logdb.DataSourceEventListener;
import org.krakenapps.logdb.DataSourceRegistry;
import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryCommand.LogMap;
import org.krakenapps.logdb.LogQueryEventListener;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.LogQueryService;
import org.krakenapps.logdb.LogQueryStatus;
import org.krakenapps.logdb.SyntaxProvider;
import org.krakenapps.logdb.mapreduce.MapQuery;
import org.krakenapps.logdb.mapreduce.MapReduceQueryStatus;
import org.krakenapps.logdb.mapreduce.MapReduceService;
import org.krakenapps.logdb.mapreduce.ReduceQuery;
import org.krakenapps.logdb.mapreduce.RemoteMapQuery;
import org.krakenapps.logdb.mapreduce.RemoteQuery;
import org.krakenapps.logdb.mapreduce.RemoteQueryKey;
import org.krakenapps.logdb.query.LogQueryImpl;
import org.krakenapps.logdb.query.StringPlaceholder;
import org.krakenapps.logdb.query.command.RpcFrom;
import org.krakenapps.logdb.query.command.RpcTo;
import org.krakenapps.rpc.RpcAgent;
import org.krakenapps.rpc.RpcClient;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcConnectionProperties;
import org.krakenapps.rpc.RpcContext;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcExceptionEvent;
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

	/**
	 * mapreduce query guid to rpcfrom mappings
	 */
	private ConcurrentMap<String, RpcFrom> rpcFromMap;

	/**
	 * mapreduce query guid to rpcto mappings
	 */
	private ConcurrentMap<String, RpcTo> rpcToMap;

	/**
	 * rpcfrom command parser
	 */
	private RpcFromParser rpcFromParser;

	/**
	 * rpcto command parser
	 */
	private RpcToParser rpcToParser;

	/**
	 * collected remote node's recent query statuses
	 */
	private ConcurrentMap<RemoteQueryKey, RemoteQuery> remoteQueries;

	/**
	 * search node connections by peer guid
	 */
	private ConcurrentMap<String, RpcConnection> upstreams;

	/**
	 * search connected remote nodes by peer guid. they push data source
	 * notifications, query status changes, and log data using separate data
	 * connection
	 */
	private ConcurrentMap<String, RpcConnection> downstreams;

	/**
	 * mapreduce query guid to map query requests (from remote node)
	 */
	private ConcurrentMap<String, MapQuery> mapQueries;

	/**
	 * mapreduce query guid to local waiting reduce queries
	 */
	private ConcurrentMap<String, ReduceQuery> reduceQueries;

	/**
	 * remote map query to mapreduce query guid relation
	 */
	private ConcurrentMap<RemoteQueryKey, String> remoteQueryMappings;

	public MapReduceRpcService() {
	}

	@Validate
	public void start() {
		queries = new ConcurrentHashMap<String, MapReduceQueryStatus>();
		rpcFromMap = new ConcurrentHashMap<String, RpcFrom>();
		rpcToMap = new ConcurrentHashMap<String, RpcTo>();
		upstreams = new ConcurrentHashMap<String, RpcConnection>();
		downstreams = new ConcurrentHashMap<String, RpcConnection>();
		mapQueries = new ConcurrentHashMap<String, MapQuery>();
		reduceQueries = new ConcurrentHashMap<String, ReduceQuery>();
		remoteQueries = new ConcurrentHashMap<RemoteQueryKey, RemoteQuery>();
		remoteQueryMappings = new ConcurrentHashMap<RemoteQueryKey, String>();

		rpcFromParser = new RpcFromParser();
		rpcToParser = new RpcToParser();
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

	private class RpcFromParser implements LogQueryParser {
		@Override
		public void addSyntax(Syntax syntax) {
			syntax.add("rpcfrom", this, k("rpcfrom"), new StringPlaceholder());
			syntax.addRoot("rpcfrom");
		}

		@Override
		public Object parse(Binding b) {
			String guid = (String) b.getChildren()[1].getValue();
			RpcFrom rpc = new RpcFrom(guid);
			rpcFromMap.put(guid, rpc);
			return rpc;
		}
	}

	private class RpcToParser implements LogQueryParser {
		@Override
		public void addSyntax(Syntax syntax) {
			syntax.add("rpcto", this, k("rpcto"), new StringPlaceholder());
			syntax.addRoot("rpcto");
		}

		@Override
		public Object parse(Binding b) {
			String guid = (String) b.getChildren()[1].getValue();
			MapQuery mq = mapQueries.get(guid);
			RpcTo rpc = new RpcTo(agent.getGuid(), mq.getConnection(), guid);
			rpcToMap.put(guid, rpc);
			return rpc;
		}
	}

	@Override
	public RpcFrom getRpcFrom(String guid) {
		return rpcFromMap.get(guid);
	}

	@Override
	public RpcTo getRpcTo(String guid) {
		return rpcToMap.get(guid);
	}

	@RpcMethod(name = "setLogStream")
	public void setLogStream(String guid) {
		RpcSession session = RpcContext.getSession();
		session.setProperty("guid", guid);
	}

	@RpcMethod(name = "push")
	public void push(Map<String, Object> data) {
		RpcSession session = RpcContext.getSession();
		String queryGuid = (String) session.getProperty("guid");
		RpcFrom rpc = rpcFromMap.get(queryGuid);
		rpc.push(new LogMap(data));

		if (logger.isDebugEnabled()) {
			String s = Primitive.stringify(data);
			logger.debug("kraken logdb: pushed [{}] data [{}]", queryGuid, s);
		}
	}

	@RpcMethod(name = "eof")
	public void eof(String queryGuid) {
		// TODO: check if all mapper queries are ended
		// for now, send eof if one mapper query is ended
		RpcSession session = RpcContext.getSession();
		String nodeGuid = session.getConnection().getPeerGuid();

		RpcFrom rpc = rpcFromMap.get(queryGuid);
		if (rpc != null)
			rpc.eof();
		else
			logger.warn("kraken logdb: rpcfrom not found for mapreduce query [{}]", queryGuid);
	}

	@RpcMethod(name = "createMapQuery")
	public int createMapQuery(String queryGuid, String query) {
		try {
			RpcSession session = RpcContext.getSession();
			String guid = session.getConnection().getPeerGuid();

			// map query should be set before rpc command parsing
			MapQuery mq = new MapQuery(guid, session.getConnection());
			mapQueries.put(queryGuid, mq);
			mq.setQuery(queryService.createQuery(query));

			logger.info("kraken logdb: created map query [{}]", queryGuid);
			return mq.getQuery().getId();
		} catch (Exception e) {
			logger.error("kraken logdb: cannot create map query", e);
			throw new RpcException(e.getMessage());
		}
	}

	@RpcMethod(name = "startMapQuery")
	public void startMapQuery(String queryGuid) {
		MapQuery mq = mapQueries.get(queryGuid);
		if (mq == null)
			throw new RpcException("mapreduce query not found: " + queryGuid);

		queryService.startQuery(mq.getQuery().getId());
		logger.info("kraken logdb: started map query [{}]", queryGuid);
	}

	@RpcMethod(name = "removeMapQuery")
	public void removeMapQuery(String queryGuid) {
		MapQuery mq = mapQueries.remove(queryGuid);
		if (mq == null)
			throw new RpcException("mapreduce query not found: " + queryGuid);

		logger.info("kraken logdb: removed map query [{}]", queryGuid);
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

		logger.info("kraken logdb: query status change [{}, status={}]", key, status);

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
	public MapReduceQueryStatus createQuery(String queryString) {
		String queryGuid = UUID.randomUUID().toString();
		LogQuery lq = new LogQueryImpl(syntaxProvider, queryString);

		boolean foundReducer = false;
		List<LogQueryCommand> mapCommands = new ArrayList<LogQueryCommand>();
		List<LogQueryCommand> reduceCommands = new ArrayList<LogQueryCommand>();

		for (LogQueryCommand c : lq.getCommands()) {
			if (c.isReducer())
				foundReducer = true;

			if (foundReducer)
				reduceCommands.add(c);
			else
				mapCommands.add(c);
		}

		String mapQueryString = buildQueryString(mapCommands);
		String reduceQueryString = buildQueryString(reduceCommands);

		mapQueryString = mapQueryString + "|rpcto " + queryGuid;
		reduceQueryString = "rpcfrom " + queryGuid + "|" + reduceQueryString;

		logger.trace("kraken logdb: map query [{}]", mapQueryString);
		logger.trace("kraken logdb: reduce query [{}]", reduceQueryString);

		// create map queries
		List<RemoteMapQuery> mapQueries = new ArrayList<RemoteMapQuery>();
		for (RpcConnection c : downstreams.values()) {
			RpcSession session = null;
			try {
				session = c.createSession("logdb-mapreduce");
				int id = (Integer) session.call("createMapQuery", queryGuid, mapQueryString);
				mapQueries.add(new RemoteMapQuery(queryGuid, c.getPeerGuid(), id));
				remoteQueryMappings.put(new RemoteQueryKey(c.getPeerGuid(), id), queryGuid);
			} catch (Exception e) {
				logger.error("kraken logdb: cannot create mapquery", e);
			} finally {
				if (session != null)
					session.close();
			}
		}

		// create and start reduce query
		LogQuery q = queryService.createQuery(reduceQueryString);
		ReduceQuery r = new ReduceQuery(queryGuid, q);
		reduceQueries.put(queryGuid, r);

		// add to mapreduce query table
		MapReduceQueryStatus status = new MapReduceQueryStatus(queryGuid, queryString, mapQueries, r);
		queries.put(queryGuid, status);
		return status;
	}

	private String buildQueryString(List<LogQueryCommand> commands) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (LogQueryCommand c : commands) {
			if (i++ != 0)
				sb.append("|");

			sb.append(c.getQueryString());
		}

		return sb.toString();
	}

	@Override
	public void startQuery(String guid) {
		// start reduce query
		ReduceQuery r = reduceQueries.get(guid);
		queryService.startQuery(r.getQuery().getId());

		// start map queries (pumping)
		for (RpcConnection c : downstreams.values()) {
			RpcSession session = null;
			try {
				session = c.createSession("logdb-mapreduce");
				session.call("startMapQuery", guid);
			} catch (Exception e) {
				logger.error("kraken logdb: cannot start mapquery", e);
			} finally {
				if (session != null)
					session.close();
			}
		}
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
		if (!upstreams.containsKey(conn.getPeerGuid()) && !downstreams.containsKey(conn.getPeerGuid())) {
			downstreams.put(conn.getPeerGuid(), conn);
			logger.info("kraken logdb: downstream connection [{}] opened", conn);
		}
	}

	@Override
	public void connectionClosed(RpcConnection conn) {
		boolean removed = false;
		removed |= upstreams.remove(conn.getPeerGuid()) != null;
		removed |= downstreams.remove(conn.getPeerGuid()) != null;

		if (removed)
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
				conn.bind("logdb-mapreduce", this);

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
				logger.info("kraken logdb: notified query status [id={}, status={}] to peer [{}]", new Object[] { id, status,
						conn.getPeerGuid() });
			} catch (Exception e) {
				logger.warn("kraken logdb: cannot update datasource info", e);
			} finally {
				if (session != null)
					session.close();
			}
		}
	}

	@Override
	public void exceptionCaught(RpcExceptionEvent e) {
		logger.error("kraken logdb: mapreduce rpc fail", e);
	}

}
