package org.krakenapps.logdb.query.command;

import java.util.Map;
import org.krakenapps.api.Primitive;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.rpc.RpcClient;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcConnectionProperties;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rpc extends LogQueryCommand {
	private final Logger logger = LoggerFactory.getLogger(Rpc.class.getName());

	private String agentGuid;
	private RpcConnection upstream;

	/**
	 * dist query guid
	 */
	private String guid;

	private boolean sender;

	private RpcConnection datastream;
	private RpcSession datasession;

	public Rpc(String agentGuid, RpcConnection upstream, String guid, boolean sender) {
		this.agentGuid = agentGuid;
		this.upstream = upstream;
		this.guid = guid;
		this.sender = sender;
	}

	@Override
	public void push(Map<String, Object> m) {
		if (sender) {
			if (datastream == null) {
				RpcClient client = new RpcClient(agentGuid);
				RpcConnectionProperties props = new RpcConnectionProperties(upstream.getRemoteAddress());
				props.setPassword((String) upstream.getProperty("password"));
				datastream = client.connect(props);
				try {
					datasession = datastream.createSession("logdb-mapreduce");
					datasession.call("setLogStream", guid);
				} catch (RpcException e) {
					logger.error("kraken logdb: cannot set log stream", e);
				} catch (InterruptedException e) {
					logger.error("kraken logdb: cannot set log stream", e);
				}

				logger.info("kraken logdb: opened rpc data stream for query guid [{}]", guid);
			}

			datasession.post("push", m);
		} else {
			write(m);
		}

		if (logger.isDebugEnabled())
			logger.debug("kraken logdb: rpc [{}]", Primitive.stringify(m));
	}

	@Override
	public boolean isReducer() {
		return false;
	}

	@Override
	public void start() {
		status = Status.Running;
	}

	@Override
	public void eof() {
		if (datasession != null) {
			datastream.close();
			datastream = null;
			datasession = null;
		}

		logger.info("kraken logdb: closed rpc data stream for query guid [{}]", guid);
		super.eof();
	}

	@Override
	public String toString() {
		return "RPC " + (sender ? "Output" : "Input") + guid;
	}

}
