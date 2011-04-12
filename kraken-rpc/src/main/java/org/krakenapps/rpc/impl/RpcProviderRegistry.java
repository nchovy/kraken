package org.krakenapps.rpc.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.krakenapps.rpc.RpcService;

public class RpcProviderRegistry {
	private Map<String, RpcService> providerMap;

	public RpcProviderRegistry() {
		providerMap = new ConcurrentHashMap<String, RpcService>();
	}
	
	public RpcService find(String name) {
		return providerMap.get(name);
	}
	
	public void register(String name, RpcService provider) {
		if (providerMap.containsKey(name))
			throw new IllegalStateException("duplicated session name");
		
		providerMap.put(name, provider);
	}
	
	public void unregister(String name) {
		providerMap.remove(name);
	}
}
