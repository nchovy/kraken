package org.krakenapps.logdb.query.command;

import org.krakenapps.api.Primitive;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.rpc.RpcClient;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcConnectionProperties;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcTo extends LogQueryCommand {
	private final Logger logger = LoggerFactory.getLogger(RpcTo.class.getName());

	private String agentGuid;
	private RpcConnection upstream;

	/**
	 * mapreduce query guid
	 */
	private String guid;

	private RpcClient client;
	private RpcConnection datastream;
	private RpcSession datasession;

	public RpcTo(String agentGuid, RpcConnection upstream, String guid) {
		this.agentGuid = agentGuid;
		this.upstream = upstream;
		this.guid = guid;
	}

	@Override
	public void push(LogMap m) {

		if (datastream == null) {
			client = new RpcClient(agentGuid);
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

		if (logger.isDebugEnabled())
			logger.debug("kraken logdb: rpc mapper [{}]", Primitive.stringify(m));
	}

	@Override
	public boolean isReducer() {
		return false;
	}

	@Override
	public void eof() {
		this.status = Status.Finalizing;
		if (datasession != null) {
			try {
				datasession.call("eof", guid);
			} catch (Exception e) {
				logger.error("kraken logdb: eof fail for mapreduce query " + guid, e);
			}

			datasession.close();
			datastream.close();
			client.close();
			client = null;
			datastream = null;
			datasession = null;
		}

		logger.info("kraken logdb: closed rpc mapper stream for query guid [{}]", guid);
		super.eof();
	}

	@Override
	public String toString() {
		return "RPC Mapper [" + guid + "]";
	}
}
