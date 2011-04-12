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
package org.krakenapps.servlet.json.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.http.HttpServiceManager;
import org.krakenapps.http.KrakenHttpService;
import org.krakenapps.servlet.json.JsonHttpMapping;
import org.krakenapps.servlet.json.JsonHttpMethod;
import org.krakenapps.servlet.json.JsonHttpServiceApi;
import org.krakenapps.servlet.json.JsonHttpServlet;
import org.krakenapps.servlet.json.JsonHttpService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "json-http-service-api")
@Provides(specifications = { JsonHttpServiceApi.class })
public class JsonHttpServiceApiImpl extends ServiceTracker implements JsonHttpServiceApi {
	final Logger logger = LoggerFactory.getLogger(JsonHttpServiceApiImpl.class.getName());

	private BundleContext bc;

	@Requires
	private HttpServiceManager httpServiceManager;

	private ConcurrentMap<String, JsonHttpService> serviceMap;

	/*
	 * key: filter id, value: JsonMethod list
	 */
	private Map<String, List<String>> methodMap;

	public JsonHttpServiceApiImpl(BundleContext bc) {
		super(bc, JsonHttpService.class.getName(), null);
		this.bc = bc;
	}

	@Validate
	@SuppressWarnings("unused")
	private void start() {
		serviceMap = new ConcurrentHashMap<String, JsonHttpService>();
		methodMap = new ConcurrentHashMap<String, List<String>>();

		try {
			ServiceReference[] refs = bc.getServiceReferences(JsonHttpService.class.getName(), null);
			if (refs != null) {
				for (ServiceReference ref : refs) {
					JsonHttpService service = (JsonHttpService) bc.getService(ref);
					if (service == null)
						continue;

					addJsonMapping(service);
				}
			}
		} catch (InvalidSyntaxException e) {
		}

		open();
	}

	@SuppressWarnings("unused")
	@Invalidate
	private void stop() {
		close();

		serviceMap = null;
		methodMap = null;
	}

	private void addJsonMapping(JsonHttpService service) {
		List<String> jsonMethods = new ArrayList<String>();
		Method[] methods = service.getClass().getMethods();
		for (Method method : methods) {
			JsonHttpMethod m = method.getAnnotation(JsonHttpMethod.class);
			if (m != null)
				jsonMethods.add(method.getName());
		}

		if (jsonMethods.size() > 0) {
			serviceMap.put(service.getName(), service);
			methodMap.put(service.getName(), jsonMethods);
		}
	}

	private void removeJsonMapping(JsonHttpService service) {
		serviceMap.remove(service.getName());
		methodMap.remove(service.getName());
	}

	@Override
	public void registerServlet(String serverId, String pathSpec) throws ServletException, NamespaceException {
		KrakenHttpService server = httpServiceManager.getHttpService(serverId);
		server.registerServlet(pathSpec, new JsonHttpServlet(this), null, null);
	}

	@Override
	public void unregisterServlet(String serverId) {
	}

	@Override
	public List<JsonHttpMapping> getFilterMappings(JsonHttpMapping criteria) {
		return null;
	}

	@Override
	public Object invokeJsonMethod(String filterId, String methodName, Map<String, Object> params) throws Exception {
		List<String> jsonMethods = methodMap.get(filterId);
		if (jsonMethods == null) {
			System.out.println("json method not found");
			return null;
		}

		if (!jsonMethods.contains(methodName)) {
			System.out.println("json method name [" + methodName + "] not found");
			return null;
		}

		JsonHttpService service = serviceMap.get(filterId);
		if (service == null) {
			System.out.println("json http service [" + filterId + "] not found");
			return null;
		}

		logger.trace("calling json method: " + methodName);
		Method method = service.getClass().getMethod(methodName, new Class[] { Map.class });
		return method.invoke(service, new Object[] { params });
	}

	@Override
	public Object addingService(ServiceReference reference) {
		JsonHttpService service = (JsonHttpService) super.addingService(reference);
		logger.info("kraken json servlet: json webservice loaded: " + service.getName());
		addJsonMapping(service);
		return service;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		JsonHttpService json = (JsonHttpService) service;
		logger.info("unloading json webservice: " + json.getName());
		removeJsonMapping(json);
		super.removedService(reference, service);
	}

	@Override
	public Map<String, List<String>> getAvailableServices() {
		return new HashMap<String, List<String>>(methodMap);
	}
}
