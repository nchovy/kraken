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
package org.krakenapps.sentry.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcMethod;
import org.krakenapps.rpc.RpcSession;
import org.krakenapps.rpc.RpcSessionEvent;
import org.krakenapps.rpc.SimpleRpcService;
import org.krakenapps.sentry.SentryRpcService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "sentry-rpc-service")
@Provides
public class SentryRpcServiceImpl extends SimpleRpcService implements SentryRpcService {
	private final Logger logger = LoggerFactory.getLogger(SentryRpcServiceImpl.class.getName());
	private CommandHandlerTracker tracker;
	private ConcurrentMap<Object, AtomicInteger> serviceSet;
	private ConcurrentMap<String, Binding> rpcMap;
	private Set<String> features;

	public SentryRpcServiceImpl(BundleContext bc) {
		tracker = new CommandHandlerTracker(this, bc);
		rpcMap = new ConcurrentHashMap<String, Binding>();
		serviceSet = new ConcurrentHashMap<Object, AtomicInteger>();
		features = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	}

	@Validate
	public void start() {
		tracker.open();
	}

	@Invalidate
	public void stop() {
		tracker.close();
	}

	@Override
	public Collection<String> getFeatures() {
		return Collections.unmodifiableCollection(features);
	}

	@Override
	public void registerFeature(String feature) {
		features.add(feature);
	}

	@Override
	public void unregisterFeature(String feature) {
		features.remove(feature);
	}

	@Override
	public Collection<String> getMethods() {
		return new ArrayList<String>(rpcMap.keySet());
	}

	@Override
	public void register(String rpcMethodName, Object service, String methodName) {
		Binding old = rpcMap.putIfAbsent(rpcMethodName, new Binding(service, methodName));
		if (old != null)
			throw new IllegalStateException("sentry rpc name is already registered: " + rpcMethodName);

		AtomicInteger counter = new AtomicInteger(0);
		AtomicInteger oldCounter = serviceSet.putIfAbsent(service, counter);
		if (oldCounter != null)
			counter = oldCounter;

		counter.incrementAndGet();

		logger.trace("kraken sentry: {} rpc method is registered", rpcMethodName);
	}

	@Override
	public void unregister(String rpcMethodName, Object service, String methodName) {
		if (!rpcMap.containsKey(rpcMethodName))
			return;

		AtomicInteger counter = serviceSet.get(service);
		int refCount = counter.decrementAndGet();
		if (refCount == 0)
			serviceSet.remove(service);

		Binding binding = rpcMap.get(rpcMethodName);
		if (binding.service == service && binding.methodName.equals(methodName)) {
			rpcMap.remove(rpcMethodName);
			logger.trace("kraken sentry: {} rpc method is unregistered", rpcMethodName);
		}
	}

	@Override
	public void sessionClosed(RpcSessionEvent e) {
		for (Object service : serviceSet.keySet()) {
			try {
				Method m = service.getClass().getMethod("sessionClosed", RpcSession.class);
				m.invoke(service, e.getSession());
			} catch (NoSuchMethodException e1) {
				// ignore
			} catch (Exception e1) {
				logger.warn("cannot invoke sessionClosed callback", e1);
			}
		}
	}

	@RpcMethod(name = "run")
	public Object run(String methodName, Object[] params) {
		if (methodName.equals("getFeatures"))
			return new ArrayList<String>(features);

		if (logger.isDebugEnabled())
			logger.debug("kraken sentry: call sentry method [{}]", methodName);

		Binding b = rpcMap.get(methodName);
		if (b == null)
			throw new RpcException("sentry method not found: " + methodName);

		try {
			Method[] methods = b.service.getClass().getMethods();
			Method m = null;

			for (Method method : methods) {
				if (method.getName().equals(methodName)) {
					m = method;
					break;
				}
			}

			if (m == null)
				throw new NoSuchMethodException(methodName);

			return m.invoke(b.service, params);
		} catch (SecurityException e) {
			logger.error("kraken sentry: sentry rpc failed", e);
			throw new RpcException("security exception: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			logger.error("kraken sentry: sentry rpc failed", e);
			throw new RpcException("method not found: " + methodName);
		} catch (IllegalArgumentException e) {
			logger.error("kraken sentry: sentry rpc failed", e);
			throw new RpcException("illegal argument: " + e.getMessage());
		} catch (IllegalAccessException e) {
			logger.error("kraken sentry: sentry rpc failed", e);
			throw new RpcException("illegal access: " + e.getMessage());
		} catch (InvocationTargetException e) {
			logger.error("kraken sentry: sentry rpc failed", e.getTargetException());
			throw new RpcException(e.getTargetException().getMessage());
		} catch (Exception e) {
			logger.error("kraken sentry: sentry rpc failed", e);
			throw new RpcException(e.getMessage());
		}
	}

	private static class Binding {
		public Object service;
		public String methodName;

		public Binding(Object service, String methodName) {
			this.service = service;
			this.methodName = methodName;
		}
	}
}
