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
package org.krakenapps.servlet.xmlrpc.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpService;
import org.krakenapps.servlet.xmlrpc.XmlRpcHttpService;
import org.krakenapps.servlet.xmlrpc.XmlRpcMethod;
import org.krakenapps.servlet.xmlrpc.XmlRpcMethodRegistry;
import org.krakenapps.servlet.xmlrpc.XmlRpcServlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "xmlrpc-method-registry")
@Provides
public class XmlRpcMethodRegistryImpl extends ServiceTracker implements XmlRpcMethodRegistry {
	private final Logger logger = LoggerFactory.getLogger(XmlRpcMethodRegistryImpl.class.getName());

	@Requires
	private HttpService httpd;

	private ConcurrentMap<String, Object[]> methodMap;

	public XmlRpcMethodRegistryImpl(BundleContext bc) throws InvalidSyntaxException {
		super(bc, XmlRpcHttpService.class.getName(), null);
		methodMap = new ConcurrentHashMap<String, Object[]>();
	}

	@Validate
	public void start() {
		// start service tracker
		open();

		HttpContext context = httpd.ensureContext("xmlrpc");
		context.addServlet("xmlrpc", new XmlRpcServlet(this), "/xmlrpc");
	}

	@Invalidate
	public void stop() {
		if (httpd != null) {
			HttpContext context = httpd.ensureContext("xmlrpc");
			context.removeServlet("xmlrpc");
		}

		// stop service tracker
		close();
	}

	@Override
	public Object addingService(ServiceReference reference) {
		Object service = super.addingService(reference);
		String instanceName = (String) reference.getProperty("instance.name");

		// inspect and register xml methods
		Map<String, String> methodNames = inspectAnnotation(service);
		for (String methodName : methodNames.keySet()) {
			logger.info("xmlrpc method registry: [{}], [{}] method registered", instanceName, methodName);
			Object[] key = new Object[2];
			key[0] = service;
			key[1] = methodNames.get(methodName);
			methodMap.put(methodName, key);
		}

		return service;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		String instanceName = (String) reference.getProperty("instance.name");

		// remove registered xml methods
		Map<String, String> methodNames = inspectAnnotation(service);
		for (String methodName : methodNames.keySet()) {
			logger.info("xmlrpc method registry: [{}], [{}] method unregistered", instanceName, methodName);
			methodMap.remove(methodName);
		}

		super.removedService(reference, service);
	}

	private Map<String, String> inspectAnnotation(Object service) {
		Map<String, String> methodNames = new HashMap<String, String>();
		Method[] methods = service.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			XmlRpcMethod xmlRpcMethod = m.getAnnotation(XmlRpcMethod.class);
			if (xmlRpcMethod == null)
				continue;

			String methodName = xmlRpcMethod.alias() + "." + xmlRpcMethod.method();
			methodNames.put(methodName, m.getName());
		}

		return methodNames;
	}

	@Override
	public Object dispatch(String signature, Object[] parameters) throws Exception {
		Object service = methodMap.get(signature)[0];
		String method = (String) methodMap.get(signature)[1];
		Method m = service.getClass().getMethod(method, getTypeArray(parameters));
		Object obj = null;
		try {
			obj = m.invoke(service, parameters);
		} catch (InvocationTargetException e) {
			logger.error("kraken-xmlrpc-servlet: invocation target error.", e);
			if (e.getCause() instanceof Exception)
				throw (Exception) e.getCause();
		}
		return obj;
	}

	private Class<?>[] getTypeArray(Object[] parameters) {
		Class<?>[] types = new Class<?>[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			types[i] = parameters[i].getClass();
			if (parameters[i] instanceof Map<?, ?>) {
				types[i] = Map.class;
			}
		}
		return types;
	}

	@Override
	public Collection<String> getMethods() {
		return methodMap.keySet();
	}

}
