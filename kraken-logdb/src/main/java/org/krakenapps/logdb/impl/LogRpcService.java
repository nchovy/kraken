package org.krakenapps.logdb.impl;

import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.rpc.RpcMethod;
import org.krakenapps.rpc.SimpleRpcService;

@Component(name = "logdb-rpc")
@Provides
public class LogRpcService extends SimpleRpcService {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "rpc.name", value = "logdb")
	private String name;

	@RpcMethod(name = "getQueries")
	public List<Object> getQueries() {
		return null;
	}

	@RpcMethod(name = "createQuery")
	public int createQuery(String query) {
		return query.length();
	}

	@RpcMethod(name = "removeQuery")
	public void removeQuery(int id) {
	}

	@RpcMethod(name = "startQuery")
	public void startQuery(int id, int offset, int limit, Integer timelineLimit) {

	}

	@RpcMethod(name = "getResult")
	public Map<String, Object> getResult(int id, int offset, int limit) {
		return null;
	}

}
