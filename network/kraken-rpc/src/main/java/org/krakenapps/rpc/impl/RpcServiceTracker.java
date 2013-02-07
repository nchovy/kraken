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

import java.util.Collection;

import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcService;
import org.krakenapps.rpc.RpcServiceBinding;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcServiceTracker implements ServiceListener {
	private final Logger logger = LoggerFactory.getLogger(RpcServiceTracker.class.getName());
	private BundleContext bc;
	private RpcHandler handler;

	public RpcServiceTracker(BundleContext bc, RpcHandler handler) {
		this.bc = bc;
		this.handler = handler;
	}

	public void scan() {
		try {
			ServiceReference[] refs = bc.getAllServiceReferences(RpcService.class.getName(), null);
			if (refs == null)
				return;

			for (ServiceReference ref : refs) {
				serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, ref));
			}
		} catch (InvalidSyntaxException e) {
		}
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		ServiceReference reference = event.getServiceReference();
		Object service = bc.getService(reference);
		if (!(service instanceof RpcService))
			return;

		String className = service.getClass().getName();

		logger.trace("kraken-rpc: inspecting service {}, event {}", className, event.getType());

		String rpcName = (String) reference.getProperty("rpc.name");
		if (rpcName == null)
			return;

		Collection<RpcConnection> connections = handler.getConnections();

		if (event.getType() == ServiceEvent.REGISTERED) {
			handler.addService((RpcService) service, rpcName);

			for (RpcConnection conn : connections) {
				try {
					logger.info("kraken-rpc: auto-binding [{}] as [{}] service for connection [{}:{}]", new Object[] {
							className, rpcName, conn.getId(), conn.getRemoteAddress() });
					conn.bind(rpcName, (RpcService) service);
				} catch (Exception e) {
					logger.warn("kraken-rpc: [" + className + "] auto-binding failed", e);
				}
			}
		} else if (event.getType() == ServiceEvent.UNREGISTERING) {
			handler.removeService((RpcService) service);

			for (RpcConnection conn : connections) {
				try {
					RpcServiceBinding binding = conn.findServiceBinding(rpcName);
					if (binding == null)
						continue;

					// if service references are same, unbind it.
					if (binding.getService() == service) {
						conn.unbind(rpcName);
						logger.info("kraken-rpc: [{} ({})] service unbound for connection [{}:{}]", new Object[] {
								rpcName, className, conn.getId(), conn.getRemoteAddress() });
					}

				} catch (Exception e) {
					logger.error("kraken-rpc: [" + className + "] auto-binding for connection " + conn.getId()
							+ " failed", e);
				}
			}
		}
	}
}
