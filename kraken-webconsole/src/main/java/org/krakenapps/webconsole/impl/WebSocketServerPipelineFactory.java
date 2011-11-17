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
package org.krakenapps.webconsole.impl;

import javax.net.ssl.SSLEngine;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.ssl.SslHandler;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.servlet.api.ServletRegistry;
import org.krakenapps.webconsole.CometSessionStore;
import org.krakenapps.webconsole.WebSocketServerParams;
import org.osgi.framework.BundleContext;

public class WebSocketServerPipelineFactory implements ChannelPipelineFactory {
	private BundleContext bc;
	private MessageBus msgbus;
	private ServletRegistry staticResourceApi;
	private CometSessionStore comet;
	private WebSocketServerParams params;

	public WebSocketServerPipelineFactory(BundleContext bc, MessageBus msgbus, ServletRegistry staticResourceApi,
			CometSessionStore comet, WebSocketServerParams params) {
		this.bc = bc;
		this.msgbus = msgbus;
		this.staticResourceApi = staticResourceApi;
		this.comet = comet;
		this.params = params;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		if (params.isSsl()) {
			SSLEngine engine = params.getSslContext().createSSLEngine();
			engine.setUseClientMode(false);
			pipeline.addLast("ssl", new SslHandler(engine));
		}

		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("aggregator", new HttpChunkAggregator(params.getMaxContentLength()));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("handler", new WebSocketServerHandler(bc, msgbus, staticResourceApi, comet));
		return pipeline;
	}
}