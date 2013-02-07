/*
 * Copyright 2010 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.rpc.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.rpc.RpcMethod;
import org.krakenapps.rpc.RpcService;
import org.krakenapps.rpc.RpcServiceBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcServiceBindingImpl implements RpcServiceBinding {
	private final Logger logger = LoggerFactory.getLogger(RpcServiceBindingImpl.class.getName());
	private String serviceName;

	// rpc method annotation mapping
	private Map<String, Method> methodMap;
	private RpcService service;

	public RpcServiceBindingImpl(String serviceName, RpcService service) {
		this.serviceName = serviceName;
		this.service = service;
		registerMethods();
	}

	@Override
	public String getName() {
		return serviceName;
	}

	public RpcService getService() {
		return service;
	}

	public Method getMethod(String name) {
		return methodMap.get(name);
	}

	private void registerMethods() {
		methodMap = new HashMap<String, Method>();

		Method[] methods = service.getClass().getDeclaredMethods();
		for (Method m : methods) {
			RpcMethod rpcMethod = m.getAnnotation(RpcMethod.class);
			if (rpcMethod == null)
				continue;

			if (methodMap.containsKey(rpcMethod.name()))
				throw new IllegalStateException("duplicated rpc method name found: " + rpcMethod.name());

			logger.trace("kraken-rpc: binding rpc method [{}], class=[{}]", rpcMethod.name(), service.getClass()
					.getName());
			methodMap.put(rpcMethod.name(), m);
		}
	}

	@Override
	public String toString() {
		return String.format("name=%s, class=%s", getName(), service.getClass().getName());
	}
}
