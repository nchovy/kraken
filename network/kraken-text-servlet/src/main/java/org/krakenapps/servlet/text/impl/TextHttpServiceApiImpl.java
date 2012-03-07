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
package org.krakenapps.servlet.text.impl;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.http.HttpServiceManager;
import org.krakenapps.http.KrakenHttpService;
import org.krakenapps.servlet.text.TextHttpMethod;
import org.krakenapps.servlet.text.TextHttpService;
import org.krakenapps.servlet.text.TextHttpServlet;
import org.krakenapps.servlet.text.TextHttpServiceApi;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "text-http-service-api")
@Provides(specifications = { TextHttpServiceApi.class })
public class TextHttpServiceApiImpl extends ServiceTracker implements TextHttpServiceApi {
	final Logger logger = LoggerFactory.getLogger(TextHttpServiceApiImpl.class.getName());

	@Requires
	private HttpServiceManager httpServiceManager;

	private ConcurrentMap<String, TextHttpService> serviceMap;

	/*
	 * key: service name, value: TextMethod list
	 */
	private Map<String, Set<String>> methodMap;

	private BundleContext bc;

	public TextHttpServiceApiImpl(BundleContext bc) {
		super(bc, TextHttpService.class.getName(), null);
		this.bc = bc;
	}

	@SuppressWarnings("unused")
	@Validate
	private void start() {
		serviceMap = new ConcurrentHashMap<String, TextHttpService>();
		methodMap = new ConcurrentHashMap<String, Set<String>>();

		try {
			ServiceReference[] refs = bc.getServiceReferences(TextHttpService.class.getName(), null);
			if (refs != null) {
				for (ServiceReference ref : refs) {
					TextHttpService service = (TextHttpService) bc.getService(ref);
					logger.debug("[text] investigating filter: " + service.getName());
					addTextMapping(service);
				}
			}
		} catch (InvalidSyntaxException e) {
		}

		open();
	}

	@SuppressWarnings("unused")
	@Invalidate
	private void stop() {
		serviceMap.clear();
		methodMap.clear();
		close();
	}

	@Override
	public Map<String, Set<String>> getAvailableFilters() {
		return methodMap;
	}

	@Override
	public Object invokeTextMethod(String serviceName, String methodName, HttpServletRequest req) throws Exception {
		Set<String> textMethods = methodMap.get(serviceName);
		if (textMethods == null) {
			throw new NoSuchMethodException();
		}

		if (!textMethods.contains(methodName))
			throw new NoSuchMethodException();

		TextHttpService service = serviceMap.get(serviceName);
		if (service == null)
			throw new NoSuchMethodException();

		logger.debug("[text-servlet] calling text method: " + methodName);
		Method method = service.getClass().getMethod(methodName, new Class[] { HttpServletRequest.class });
		return method.invoke(service, new Object[] { req });
	}

	@Override
	public Object invokeTextMethod(String serviceName, String methodName, Map<String, Object> params) throws Exception {
		Set<String> textMethods = methodMap.get(serviceName);
		if (textMethods == null) {
			throw new NoSuchMethodException();
		}

		if (!textMethods.contains(methodName))
			throw new NoSuchMethodException();

		TextHttpService service = serviceMap.get(serviceName);
		if (service == null)
			throw new NoSuchMethodException();

		logger.debug("[text-servlet] calling text method: " + methodName);
		Method method = service.getClass().getMethod(methodName, new Class[] { Map.class });
		return method.invoke(service, new Object[] { params });
	}

	@Override
	public void registerServlet(String serverId, String pathSpec) throws ServletException, NamespaceException {
		KrakenHttpService server = httpServiceManager.getHttpService(serverId);
		server.registerServlet(pathSpec, new TextHttpServlet(this), null, null);
	}

	private void addTextMapping(TextHttpService service) {
		Set<String> textMethods = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		Method[] methods = service.getClass().getMethods();
		for (Method method : methods) {
			TextHttpMethod m = method.getAnnotation(TextHttpMethod.class);
			if (m != null) {
				logger.trace("[text] registering filter method: " + method.getName());
				textMethods.add(method.getName());
			}
		}

		if (textMethods.size() > 0) {
			serviceMap.put(service.getName(), service);
			methodMap.put(service.getName(), textMethods);
		}
	}

	private void removeTextMapping(TextHttpService service) {
		methodMap.remove(service.getName());
	}

	@Override
	public Object addingService(ServiceReference reference) {
		TextHttpService service = (TextHttpService) super.addingService(reference);
		logger.trace("[text-servlet] filter loaded: " + service.getName());
		addTextMapping(service);
		return service;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		TextHttpService text = (TextHttpService) service;
		logger.trace("[text-servlet] filter loaded: " + text.getName());
		removeTextMapping(text);
		super.removedService(reference, service);
	}
}
