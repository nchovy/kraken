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

import java.lang.reflect.Method;

import org.krakenapps.sentry.SentryCommandHandler;
import org.krakenapps.sentry.SentryMethod;
import org.krakenapps.sentry.SentryRpcService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandHandlerTracker extends ServiceTracker {
	private final Logger logger = LoggerFactory.getLogger(CommandHandlerTracker.class.getName());

	private SentryRpcService rpc;

	public CommandHandlerTracker(SentryRpcService rpc, BundleContext bc) {
		super(bc, SentryCommandHandler.class.getName(), null);
		this.rpc = rpc;
	}

	@Override
	public Object addingService(ServiceReference reference) {
		Object service = super.addingService(reference);
		addFeatures(service);
		addMethods(service);
		return service;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		removeFeatures(service);
		removeMethods(service);
		super.removedService(reference, service);
	}

	private void addFeatures(Object service) {
		SentryCommandHandler handler = (SentryCommandHandler) service;
		for (String feature : handler.getFeatures())
			rpc.registerFeature(feature);
	}

	private void removeFeatures(Object service) {
		SentryCommandHandler handler = (SentryCommandHandler) service;
		for (String feature : handler.getFeatures())
			rpc.unregisterFeature(feature);
	}

	private void addMethods(Object service) {
		for (Method m : service.getClass().getMethods()) {
			SentryMethod sentryMethod = m.getAnnotation(SentryMethod.class);
			if (sentryMethod != null) {
				String rpcName = getRpcMethodName(m, sentryMethod);
				logger.trace("kraken sentry: adding rpc method [{}]", rpcName);
				try {
					rpc.register(rpcName, service, m.getName());
				} catch (IllegalStateException e) {
					logger.trace("kraken sentry: failed to add rpc method " + rpcName, e);
				}
			}
		}
	}

	private void removeMethods(Object service) {
		for (Method m : service.getClass().getMethods()) {
			SentryMethod sentryMethod = m.getAnnotation(SentryMethod.class);
			if (sentryMethod != null) {
				String rpcName = getRpcMethodName(m, sentryMethod);
				logger.trace("kraken sentry: removing rpc method [{}]", rpcName);
				rpc.unregister(rpcName, service, m.getName());
			}
		}
	}

	private String getRpcMethodName(Method m, SentryMethod sentryMethod) {
		if (sentryMethod.method() == null || sentryMethod.method().isEmpty())
			return m.getName();
		else
			return sentryMethod.method();
	}
}
