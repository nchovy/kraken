/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.slpolicy;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

@Component(name = "slpolicy-server")
@Provides
public class SilverlightPolicyServerImpl implements SilverlightPolicyServer {
	private Channel listener;

	@Override
	public boolean isOpen() {
		return listener != null;
	}

	@Validate
	@Override
	public synchronized void open() {
		if (listener != null)
			throw new IllegalStateException("tcp/943 already opened");

		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

		bootstrap.setPipelineFactory(new SilverlightPolicyPipelineFactory());
		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		InetSocketAddress addr = new InetSocketAddress(943);
		listener = bootstrap.bind(addr);
	}

	@Invalidate
	public void stop() {
		if (listener != null) {
			listener.close();
			listener = null;
		}
	}

	@Override
	public synchronized void close() {
		if (listener == null)
			throw new IllegalStateException("policy server is not running");

		listener.close();
		listener = null;
	}
}
