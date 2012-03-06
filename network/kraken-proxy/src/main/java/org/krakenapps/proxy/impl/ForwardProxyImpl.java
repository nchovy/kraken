/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.krakenapps.proxy.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.krakenapps.proxy.ForwardProxy;
import org.krakenapps.proxy.ForwardRoute;

@Component(name = "forward-proxy")
@Provides
public class ForwardProxyImpl implements ForwardProxy {
	private ExecutorService executor;
	private ConcurrentMap<String, ForwardChannel> routeMap;

	@Validate
	public void start() {
		executor = Executors.newCachedThreadPool();
		routeMap = new ConcurrentHashMap<String, ForwardChannel>();
	}

	@Invalidate
	public void stop() {
		if (routeMap != null) {
			// stop all proxies
			for (ForwardChannel channel : routeMap.values())
				channel.server.close();

			routeMap.clear();
		}

		if (executor != null) {
			executor.shutdownNow();
			executor = null;
		}
	}

	@Override
	public Collection<String> getRouteNames() {
		if (routeMap != null)
			return new ArrayList<String>(routeMap.keySet());
		return null;
	}

	@Override
	public ForwardRoute getRoute(String name) {
		if (routeMap == null)
			return null;

		ForwardChannel channel = routeMap.get(name);
		if (channel != null)
			return channel.route;
		return null;
	}

	@Override
	public void addRoute(String name, ForwardRoute route) {
		ServerBootstrap sb = new ServerBootstrap(new NioServerSocketChannelFactory(executor, executor));
		ClientSocketChannelFactory cf = new NioClientSocketChannelFactory(executor, executor);

		String remoteHost = route.getRemote().getHostName();
		int remotePort = route.getRemote().getPort();
		int localPort = route.getLocal().getPort();

		sb.setPipelineFactory(new ForwardProxyPipelineFactory(cf, remoteHost, remotePort));
		Channel server = sb.bind(new InetSocketAddress(localPort));
		routeMap.put(name, new ForwardChannel(server, route));
	}

	@Override
	public void removeRoute(String name) {
		if (routeMap.containsKey(name)) {
			ForwardChannel channel = routeMap.remove(name);
			if (channel != null)
				channel.server.close();
		}
	}

	static class ForwardChannel {
		public ForwardChannel(Channel server, ForwardRoute route) {
			this.server = server;
			this.route = route;
		}

		private ForwardRoute route;
		private Channel server;
	}
}
