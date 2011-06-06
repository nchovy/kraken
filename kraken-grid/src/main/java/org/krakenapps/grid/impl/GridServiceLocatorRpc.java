package org.krakenapps.grid.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.rpc.RpcMethod;
import org.krakenapps.rpc.SimpleRpcService;

@Component(name = "grid-service-locator")
@Provides
public class GridServiceLocatorRpc extends SimpleRpcService {
	
	@ServiceProperty(name = "rpc.name", value = "grid-locator")
	private String name;
	
	@RpcMethod(name = "getGridServices")
	public Object getGridServices() {
		return null;
	}
}
