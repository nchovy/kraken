package org.krakenapps.rpc;

import java.util.Collection;

public interface RpcBlockingTable {
	Collection<RpcWaitingCall> getWaitingCalls();
	
	void cancel(int id);

	void signal(int id, RpcMessage response);

	RpcMessage await(int id) throws InterruptedException;

	RpcMessage await(int id, long timeout) throws InterruptedException;
}
