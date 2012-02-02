package org.krakenapps.rpc;

import java.util.Collection;


public interface RpcAsyncTable {
	Collection<RpcWaitingCall> getWaitingCalls();

	boolean contains(int id);

	void cancel(int id);

	void signal(int id, RpcMessage response);

	void submit(int id, RpcAsyncResult result);
}
