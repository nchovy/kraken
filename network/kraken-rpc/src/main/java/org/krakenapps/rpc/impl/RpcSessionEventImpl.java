package org.krakenapps.rpc.impl;

import java.util.Properties;

import org.krakenapps.rpc.RpcSession;
import org.krakenapps.rpc.RpcSessionEvent;

public class RpcSessionEventImpl implements RpcSessionEvent {
	private int type;
	private RpcSession session;
	private Properties props;

	public RpcSessionEventImpl(int type, RpcSession session) {
		this(type, session, new Properties());
	}

	public RpcSessionEventImpl(int type, RpcSession session, Properties props) {
		this.session = session;
		this.props = props;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public RpcSession getSession() {
		return session;
	}

	@Override
	public Object getParameter(String key) {
		return props.get(key);
	}

}
