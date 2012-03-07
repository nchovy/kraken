package org.krakenapps.rpc.impl;

import org.krakenapps.rpc.RpcExceptionEvent;

public class RpcExceptionEventImpl implements RpcExceptionEvent {
	private Throwable t;

	public RpcExceptionEventImpl(Throwable t) {
		this.t = t;
	}

	@Override
	public Throwable getCause() {
		return t;
	}

}
