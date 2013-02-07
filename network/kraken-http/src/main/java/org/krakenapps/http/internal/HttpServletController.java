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

import java.util.HashMap;
import java.util.Random;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.InstanceStateListener;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.http.KrakenHttpService;
import org.krakenapps.http.ServletLifecycleManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provides
public class HttpServletController implements ServletLifecycleManager, InstanceStateListener {
	private final Logger logger = LoggerFactory.getLogger(HttpServletController.class);

	class ServletInfo {
		public KrakenHttpService httpServlet;
		public String alias;
		public String instanceName;

		public ServletInfo(KrakenHttpService httpServletImpl, String alias, String instanceName) {
			this.httpServlet = httpServletImpl;
			this.alias = alias;
			this.instanceName = instanceName;
		}
	}

	private HttpServletTracker servletTracker;
	private HashMap<String, ServletInfo> servlets;

	public HttpServletController(BundleContext bundleContext) {
		try {
			servletTracker = new HttpServletTracker(bundleContext, this);
			servlets = new HashMap<String, ServletInfo>();
		} catch (InvalidSyntaxException e) {
			logger.debug(e.getClass().getName(), e);
		}
	}

	@Override
	public void stateChanged(ComponentInstance instance, int arg1) {
		if (instance.getState() == ComponentInstance.DISPOSED) {
			System.out.println(instance.getInstanceName() + " - http-servlet component instance disposed.");
			unregisterServlet(instance.getInstanceName());
		}
	}

	public void start() {
		System.out.println("HttpServletController.start() called");
		servletTracker.open();
	}

	public void stop() {
		System.out.println("HttpServletController.stop() called");
		servletTracker.close();
	}

	@Override
	public void unregisterServlet(String instanceName) {
		ServletInfo servletInfo = servlets.get(instanceName);
		if (servletInfo != null) {
			servletInfo.httpServlet.unregister(servletInfo.alias);
			servlets.remove(instanceName);
		} else {
			logger.warn("Unregister servlet failed: " + instanceName);
		}
	}

	@Override
	public void registerServlet(KrakenHttpService httpService, String alias, ComponentInstance servletComponentInstance) {
		Random rand = new Random();
		int randKey = rand.nextInt();
		while (servlets.containsKey(randKey))
			randKey = rand.nextInt();
		String servletInstanceName = servletComponentInstance.getInstanceName();
		servlets.put(servletInstanceName, new ServletInfo(httpService, alias, servletInstanceName));
	}
}
