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
package org.krakenapps.httpd.impl;

import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.httpd.HttpConfiguration;
import org.krakenapps.httpd.HttpConfigurationListener;
import org.krakenapps.httpd.HttpContextRegistry;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpPipelineFactory implements ChannelPipelineFactory, HttpConfigurationListener {
	private final Logger logger = LoggerFactory.getLogger(HttpPipelineFactory.class.getName());
	private BundleContext bc;
	private HttpConfiguration config;
	private HttpContextRegistry contextRegistry;
	private KeyStoreManager keyStoreManager;
	private Timer timer;
	private IdleStateHandler idleStateHandler;

	public HttpPipelineFactory(BundleContext bc, HttpConfiguration config, HttpContextRegistry contextRegistry,
			KeyStoreManager keyStoreManager) {
		this.bc = bc;
		this.config = config;
		this.contextRegistry = contextRegistry;
		this.keyStoreManager = keyStoreManager;
		this.timer = new HashedWheelTimer();
		this.idleStateHandler = new IdleStateHandler(timer, 0, 0, config.getIdleTimeout());
		config.getListeners().add(this);
	}

	@Override
	public void onSet(String fieldName, Object value) {
		if (fieldName.equals("idleTimeout")) {
			logger.debug("kraken httpd: http config field [{}] changed to [{}]", fieldName, value);
			this.idleStateHandler = new IdleStateHandler(timer, 0, 0, config.getIdleTimeout());
		}
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline(idleStateHandler);

		SSLContext sslContext = newSslContext(config.getKeyAlias(), config.getTrustAlias());
		if (config.isSsl()) {
			SSLEngine engine = sslContext.createSSLEngine();
			engine.setUseClientMode(false);

			pipeline.addLast("ssl", new SslHandler(engine));
		}

		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("aggregator", new HttpChunkAggregator(config.getMaxContentLength()));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
		pipeline.addLast("handler", new HttpServerHandler(bc, config, contextRegistry));
		return pipeline;
	}

	private SSLContext newSslContext(String keyAlias, String trustAlias) {
		try {
			SSLContext sslContext = SSLContext.getInstance("SSL");
			TrustManager[] trustManagers = null;
			KeyManager[] keyManagers = null;

			TrustManagerFactory tmf = keyStoreManager.getTrustManagerFactory(trustAlias, "SunX509");
			KeyManagerFactory kmf = keyStoreManager.getKeyManagerFactory(keyAlias, "SunX509");

			if (tmf != null)
				trustManagers = tmf.getTrustManagers();
			if (kmf != null)
				keyManagers = kmf.getKeyManagers();

			sslContext.init(keyManagers, trustManagers, new SecureRandom());
			return sslContext;
		} catch (Exception e) {
			throw new RuntimeException("cannot create ssl context", e);
		}
	}

}