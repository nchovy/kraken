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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletContext;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.InstanceStateListener;
import org.krakenapps.http.KrakenHttpService;
import org.krakenapps.http.internal.service.HttpServiceFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServiceController implements InstanceStateListener {
	private final Logger logger = LoggerFactory.getLogger(HttpServiceController.class);

	// customized
	private HttpServiceTracker httpServiceTracker;

	private ConcurrentMap<Long, KrakenHttpService> httpServiceMap;
	private ConcurrentMap<String, Long> servletMap;
	private ConcurrentMap<String, ComponentInstance> instanceMap;
	private ConcurrentMap<String, String> aliasMap;

	// original
	private final BundleContext context;
	private final Hashtable<String, Object> serviceProps;
//	private ServiceRegistration serviceReg;
	
	// stania added
	private Map<String, ServiceRegistration> serviceRegs;

	public HttpServiceController(BundleContext bundleContext) {
		httpServiceMap = new ConcurrentHashMap<Long, KrakenHttpService>();
		servletMap = new ConcurrentHashMap<String, Long>();
		instanceMap = new ConcurrentHashMap<String, ComponentInstance>();
		aliasMap = new ConcurrentHashMap<String, String>();

		httpServiceTracker = new HttpServiceTracker(bundleContext, this);

		// original
		this.context = bundleContext;
//		this.registry = new HandlerRegistry();
//		this.dispatcher = new Dispatcher(this.registry);
		this.serviceProps = new Hashtable<String, Object>();
		
		// stania added
		this.serviceRegs = new HashMap<String, ServiceRegistration>();
	}

	public void start() {
		httpServiceTracker.open();
	}

	public void stop() {
		httpServiceTracker.close();
	}

	public void registerHttpService(long serviceId, KrakenHttpService service) {
		// called by tracker
		logger.info("new http service registered: " + serviceId);
		httpServiceMap.put(serviceId, service);
	}

	public void unregisterHttpService(long serviceId) {
		logger.info("http service unregistered: " + serviceId);

		List<String> removeList = new ArrayList<String>();

		// find remove target servlet component
		for (String instanceName : servletMap.keySet()) {
			long mappedId = servletMap.get(instanceName);
			if (serviceId == mappedId) {
				removeList.add(instanceName);
			}
		}

		// remove from map
		for (String instanceName : removeList) {
			ComponentInstance instance = instanceMap.get(instanceName);
			instance.dispose(); // unregisterServlet will be called.
		}

		httpServiceMap.remove(serviceId);
	}

	public void setProperties(Hashtable<String, Object> props) {
		this.serviceProps.clear();
		this.serviceProps.putAll(props);

		// stania: this can be different from original semantics
		// this may need servletContext to set serviceProps
		for (ServiceRegistration reg: serviceRegs.values()) {
			reg.setProperties(this.serviceProps);
		}
	}

	public void register(DispatcherServlet dispatcherServlet, ServletContext servletContext) {
		// called by dispatcher servlet
		HttpServiceFactory factory = new HttpServiceFactory(servletContext, dispatcherServlet.getHandlerRegistry());
		String[] ifaces = new String[] { HttpService.class.getName(),
				KrakenHttpService.class.getName() };

		// add httpservice.name and bundle.id attribute for http service registration
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.putAll(serviceProps);

		String serverName = (String) servletContext.getAttribute("httpservice.name");
		props.put("httpservice.name", serverName);

		// register http service (OSGi service registration)
		serviceRegs.put(serverName, this.context.registerService(ifaces, factory, props));
		
	}

	public void unregister(DispatcherServlet dispatcherServlet) {
		String serverName = (String) dispatcherServlet.getServletContext().getAttribute("httpservice.name");
		if (!serviceRegs.containsKey(serverName)) {
			return;
		}

		ServiceRegistration serviceReg = null;
		try {
			serviceReg = serviceRegs.get(serverName);
			serviceReg.unregister();
		} finally {
			if (serviceReg != null) 
				serviceRegs.remove(serverName);
		}
	}

	@Override
	public void stateChanged(ComponentInstance instance, int state) {
		if (instance.getState() == ComponentInstance.DISPOSED) {
			System.out.println(instance.getInstanceName() + " - component instance disposed.");
			unregisterServlet(instance);
		}
	}

	private void unregisterServlet(ComponentInstance instance) {
		Long httpServiceId = servletMap.get(instance.getInstanceName());
		if (httpServiceId == null) {
			System.out.println("http service not found");
			return;
		}

		servletMap.remove(instance.getInstanceName());
		instanceMap.remove(instance.getInstanceName());
		String alias = aliasMap.remove(instance.getInstanceName());

		// instance can be already unregistered when http service is unloading.
		KrakenHttpService httpService = httpServiceMap.get(httpServiceId);
		httpService.unregister(alias);
	}


}
