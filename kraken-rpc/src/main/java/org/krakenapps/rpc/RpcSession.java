package org.krakenapps.rpc;

import org.krakenapps.rpc.impl.RpcAsyncCallback;
import org.krakenapps.rpc.impl.RpcAsyncResult;

public interface RpcSession {
	RpcSessionState getState();

	int getId();

	RpcConnection getConnection();

	String getServiceName();

	Object getProperty(String name);

	void setProperty(String name, Object value);

	RpcAsyncResult call(String method, Object[] params, RpcAsyncCallback callback);

	Object call(String method, Object[] params) throws RpcException, InterruptedException;

	Object call(String method, Object[] params, long timeout) throws RpcException, InterruptedException;

	void post(String method, Object[] params);

	void close();

	void addListener(RpcSessionEventCallback callback);

	void removeListener(RpcSessionEventCallback callback);
}
