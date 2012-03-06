package org.krakenapps.rpc;

import java.util.Date;

public interface RpcWaitingCall {
	int getId();

	Date getSince();

	void done(RpcMessage result);

	void await(int timeout) throws InterruptedException;
	
	RpcMessage getResult();
}
