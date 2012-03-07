/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.http.internal;

import java.util.Map;
import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;
import org.mortbay.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JettyHttpService {
	final Logger logger = LoggerFactory.getLogger(JettyHttpService.class.getName());
	private Server server;
	private boolean isOpen;
	private Map<String, String> configMap;
	private DispatcherServlet dispatcher;
	private String httpServiceName;

	public JettyHttpService(DispatcherServlet dispatcher, HttpConfig httpConfig,
			String httpServiceName, Map<String, String> config) {
		this.dispatcher = dispatcher;
		this.configMap = config;
		this.httpServiceName = httpServiceName;
	}

	public void open() throws Exception {
		int port = getIntegerConfig(configMap, "port");
		boolean isSsl = Boolean.parseBoolean((String) configMap.get("ssl"));

		HandlerList handlers = new HandlerList();

		server = new Server(port);
		server.setHandler(handlers);

		AbstractConnector connector = new SelectChannelConnector();
		connector.setPort(port);

		ThreadPool threadPool = createThreadPool();

		if (isSsl) {
			SslSocketConnector sslConnector = new SslSocketConnector();
			logger.info("jetty ssl mode: listening {} port", port);

			sslConnector.setPort(port);
			sslConnector.setKeystore((String) configMap.get("keyStore"));
			sslConnector.setPassword((String) configMap.get("password"));
			sslConnector.setKeyPassword((String) configMap.get("keyPassword"));
			sslConnector.setTruststore((String) configMap.get("trustStore"));
			sslConnector.setTrustPassword((String) configMap.get("trustPassword"));

			connector = sslConnector;
		}

		connector.setMaxIdleTime(5000);

		// timeout in milliseconds
		if (configMap.containsKey("maxIdleTime")) {
			connector.setMaxIdleTime(getIntegerConfig(configMap, "maxIdleTime"));
			logger.info("max idle time for jetty: {} milliseconds", configMap.get("maxIdleTime"));
		}

		if (threadPool != null)
			connector.setThreadPool(threadPool);

		Integer backlog = getBacklogSize();
		if (backlog != null) {
			connector.setAcceptQueueSize(backlog);
			logger.info("backlog for jetty: {}", backlog);
		}

		server.setConnectors(new Connector[] { connector });

		// add dispatcher servlet
		Context context = new Context(this.server, "/", Context.SESSIONS);
		context.addServlet(new ServletHolder(this.dispatcher), "/*");
		context.setAttribute("httpservice.name", httpServiceName);

		server.start();
		isOpen = true;
	}

	private Integer getBacklogSize() {
		if (configMap.containsKey("backlog"))
			return Integer.parseInt(configMap.get("backlog"));

		return null;
	}

	private ThreadPool createThreadPool() {
		Integer minThreads = tryParseInt("minThread");
		Integer maxThreads = tryParseInt("maxThread");

		if (maxThreads == null && minThreads == null)
			return null;

		logger.info("use queue thread pool for jetty: min [{}] max [{}]", minThreads, maxThreads);

		QueuedThreadPool pool = new QueuedThreadPool();
		if (minThreads != null)
			pool.setMinThreads(minThreads);

		if (maxThreads != null)
			pool.setMaxThreads(maxThreads);

		return pool;
	}

	private Integer tryParseInt(String key) {
		if (configMap.containsKey(key))
			return Integer.parseInt(configMap.get(key));

		return null;
	}

	public void close() {
		try {
			if (server != null)
				server.stop();

			isOpen = false;
			server = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int getIntegerConfig(Map<String, String> config, String key) throws Exception {
		Object value = config.get(key);
		if (value == null)
			throw new Exception("[" + key + "] configuration not found.");

		return Integer.parseInt((String) value);
	}

	public Map<String, String> getConfig() {
		return configMap;
	}

	public boolean isOpen() {
		return isOpen;
	}

}
