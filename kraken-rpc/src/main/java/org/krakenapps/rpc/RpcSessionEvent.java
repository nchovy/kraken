package org.krakenapps.rpc;

public interface RpcSessionEvent {
	final int Requested = 1;
	final int Opened = 2;
	final int Closed = 3;
	
	int getType();
	
	RpcSession getSession();

	Object getParameter(String key);
}
