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
package org.krakenapps.servlet.xml.impl;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
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
import org.krakenapps.servlet.xml.XmlHttpService;
import org.krakenapps.servlet.xml.XmlHttpMethod;
import org.krakenapps.servlet.xml.XmlHttpServiceApi;
import org.krakenapps.servlet.xml.XmlHttpServlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "xml-http-service-api")
@Provides(specifications = { XmlHttpServiceApi.class })
public class XmlHttpServiceApiImpl extends ServiceTracker implements XmlHttpServiceApi {
	final Logger logger = LoggerFactory.getLogger(XmlHttpServiceApiImpl.class.getName());

	@Requires
	private HttpServiceManager httpServiceManager;

	private ConcurrentMap<String, XmlHttpService> serviceMap;
	/*
	 * key: filter id, value: XmlMethod list
	 */
	private Map<String, Set<String>> methodMap;

	private BundleContext bc;

	public XmlHttpServiceApiImpl(BundleContext bc) {
		super(bc, XmlHttpService.class.getName(), null);
		this.bc = bc;
	}

	@SuppressWarnings("unused")
	@Validate
	private void start() {
		serviceMap = new ConcurrentHashMap<String, XmlHttpService>();
		methodMap = new ConcurrentHashMap<String, Set<String>>();

		try {
			ServiceReference[] refs = bc.getServiceReferences(XmlHttpService.class.getName(), null);
			if (refs != null) {
				for (ServiceReference ref : refs) {
					XmlHttpService service = (XmlHttpService) bc.getService(ref);
					logger.debug("[xml-servlet] investigating service: " + service.getName());
					addXmlMapping(service);
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
	public void registerServlet(String serverId, String pathSpec) throws ServletException, NamespaceException {
		KrakenHttpService http = httpServiceManager.getHttpService(serverId);
		http.registerServlet(pathSpec, new XmlHttpServlet(this), null, null);
	}

	private void addXmlMapping(XmlHttpService service) {
		Set<String> xmlMethods = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		Method[] methods = service.getClass().getMethods();
		for (Method method : methods) {
			XmlHttpMethod m = method.getAnnotation(XmlHttpMethod.class);
			if (m != null) {
				xmlMethods.add(method.getName());
			}
		}

		if (xmlMethods.size() > 0) {
			serviceMap.put(service.getName(), service);
			methodMap.put(service.getName(), xmlMethods);
		}
	}

	private void removeXmlMapping(XmlHttpService service) {
		methodMap.remove(service.getName());
	}

	@Override
	public Object addingService(ServiceReference reference) {
		XmlHttpService svc = (XmlHttpService) super.addingService(reference);
		logger.trace("[xml-servlet] xml http service loaded: " + svc.getName());
		addXmlMapping(svc);
		return svc;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		XmlHttpService svc = (XmlHttpService) service;
		logger.trace("[xml-servlet] xml http service unloaded: " + svc.getName());
		removeXmlMapping(svc);
		super.removedService(reference, service);
	}

	@Override
	public Map<String, Set<String>> getXmlHttpMappings() {
		return methodMap;
	}

	@Override
	public Object invokeXmlMethod(String serviceName, String methodName, Map<String, Object> params) throws Exception {
		Set<String> xmlMethods = methodMap.get(serviceName);
		if (xmlMethods == null) {
			return null;
		}

		if (!xmlMethods.contains(methodName))
			throw new NoSuchMethodException("no such method");

		XmlHttpService service = serviceMap.get(serviceName);
		if (service == null)
			return null;

		logger.debug("[xml-servlet] calling xml method {}", methodName);
		Method method = service.getClass().getMethod(methodName, new Class[] { Map.class });
		return method.invoke(service, new Object[] { params });
	}

}
