package org.krakenapps.rpc;

import java.lang.reflect.Method;

public interface RpcServiceBinding {
	String getName();
	
	RpcService getService();

	Method getMethod(String name);
}
