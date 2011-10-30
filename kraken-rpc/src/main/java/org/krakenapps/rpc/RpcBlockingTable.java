package org.krakenapps.rpc;

import java.util.Collection;

public interface RpcBlockingTable {
	Collection<RpcWaitingCall> getWaitingCalls();

	void cancel(int id);

	void signal(int id, RpcMessage response);

	RpcWaitingCall set(int id);

	RpcMessage await(RpcWaitingCall item) throws InterruptedException;

	RpcMessage await(RpcWaitingCall item, long timeout) throws InterruptedException;
}
