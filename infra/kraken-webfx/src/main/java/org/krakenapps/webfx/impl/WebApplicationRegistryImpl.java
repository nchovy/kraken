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
package org.krakenapps.webfx.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpService;
import org.krakenapps.webfx.DispatcherServlet;
import org.krakenapps.webfx.Resource;
import org.krakenapps.webfx.WebApplication;
import org.krakenapps.webfx.WebApplicationRegistry;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "webfx-app-registry")
@Provides
public class WebApplicationRegistryImpl implements WebApplicationRegistry {
	private final Logger logger = LoggerFactory.getLogger(WebApplicationRegistryImpl.class.getName());

	@Requires
	private HttpService httpd;

	@Requires
	private ConfigService conf;

	private BundleContext bc;

	private ConcurrentMap<String, WebApplication> apps;

	public WebApplicationRegistryImpl(BundleContext bc) {
		this.bc = bc;
		this.apps = new ConcurrentHashMap<String, WebApplication>();
	}

	@Validate
	public void start() {
		reload();
	}

	@Invalidate
	public void stop() {

	}

	@Override
	public List<WebApplication> getWebApplications() {
		return new ArrayList<WebApplication>(apps.values());
	}

	@Override
	public WebApplication getWebApplication(String name) {
		return apps.get(name);
	}

	@Override
	public void createWebApplication(WebApplication app) {
		WebApplication old = apps.putIfAbsent(app.getContext(), app);

		if (old != null)
			throw new IllegalStateException("context already used by others: " + app.getContext());

		ConfigDatabase db = conf.ensureDatabase("kraken-webfx");
		db.add(app, "kraken-webfx", "created new web application");
		
		start(app);
	}

	@Override
	public void updateWebApplication(WebApplication app) {
		WebApplication old = apps.get(app.getContext());
		if (old == null)
			throw new IllegalStateException("context not found: " + app.getContext());

		ConfigDatabase db = conf.ensureDatabase("kraken-webfx");
		Config c = db.findOne(WebApplication.class, Predicates.field("context", app.getContext()));
		if (c == null)
			throw new IllegalStateException("config not found: " + app.getContext());

		db.update(c, app, false, "kraken-webfx", "updated web application: " + app.getContext());
	}

	@Override
	public void removeWebApplication(String name) {
		WebApplication old = apps.remove(name);
		if (old == null)
			throw new IllegalStateException("context not found: " + name);

		ConfigDatabase db = conf.ensureDatabase("kraken-webfx");
		Config c = db.findOne(WebApplication.class, Predicates.field("context", name));
		if (c == null)
			throw new IllegalStateException("config not found: " + name);

		db.remove(c, false, "kraken-webfx", "removed web application: " + name);
	}

	@Override
	public void reload() {
		// load configs
		ConfigDatabase db = conf.ensureDatabase("kraken-webfx");
		ConfigIterator it = db.findAll(WebApplication.class);
		for (WebApplication app : it.getDocuments(WebApplication.class)) {
			WebApplication old = apps.putIfAbsent(app.getContext(), app);
			if (old != null) {
				logger.warn("kraken webfx: context [{}] already exists, skipping", app.getContext());
				continue;
			}

			start(app);
		}
		it.close();
	}

	private void start(WebApplication app) {
		DispatcherServlet dispatcher = new DispatcherServlet(bc, new File(app.getRootPath()));

		HttpContext ctx = httpd.ensureContext(app.getContext());
		ServletContext servletContext = ctx.getServletContext();
		servletContext.addServlet("dispatcher", dispatcher);
		ServletRegistration reg = servletContext.getServletRegistration("dispatcher");
		reg.addMapping("/*");

		try {
			for (Resource r : app.getResources())
				dispatcher.getRouter().add(r);

			dispatcher.start();
		} catch (Throwable t) {
			logger.error("kraken webfx: cannot start webapp - " + app.getContext(), t);
		}
	}
}
