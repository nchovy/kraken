/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.http.internal;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;

import org.krakenapps.http.internal.dispatch.Dispatcher;
import org.krakenapps.http.internal.handler.HandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class DispatcherServlet extends HttpServlet {
	private final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class.getName());
	private static final long serialVersionUID = 1L;
	private final HttpServiceController controller;
	private String httpServiceName;
	private final HandlerRegistry registry;
	private final Dispatcher dispatcher;

	public HandlerRegistry getHandlerRegistry() {
		return registry;
	}

	public DispatcherServlet(HttpServiceController controller) {
		this.controller = controller;
		this.registry = new HandlerRegistry();
		this.dispatcher = new Dispatcher(this.registry);
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.controller.register(this, getServletContext());
		
		httpServiceName = (String) getServletContext().getAttribute("httpservice.name");
		logger.info("kraken http: init dispatcher servlet for [{}] server", httpServiceName);
	}

	@Override
	public void destroy() {
		logger.info("kraken http: destroy dispatcher servlet of [{}] server", httpServiceName);
		
		this.controller.unregister(this);
		this.registry.removeAll();
		super.destroy();
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		this.dispatcher.dispatch(req, res);
	}
}
