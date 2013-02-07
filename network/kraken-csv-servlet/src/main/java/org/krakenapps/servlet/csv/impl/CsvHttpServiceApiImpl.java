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
package org.krakenapps.servlet.csv.impl;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
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
import org.krakenapps.servlet.csv.CsvHttpMethod;
import org.krakenapps.servlet.csv.CsvHttpService;
import org.krakenapps.servlet.csv.CsvHttpServlet;
import org.krakenapps.servlet.csv.CsvRow;
import org.krakenapps.servlet.csv.CsvHttpServiceApi;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "csv-http-service-api")
@Provides(specifications = { CsvHttpServiceApi.class })
public class CsvHttpServiceApiImpl extends ServiceTracker implements CsvHttpServiceApi {
	final Logger logger = LoggerFactory.getLogger(CsvHttpServiceApiImpl.class.getName());

	@Requires
	private HttpServiceManager httpServiceManager;

	private BundleContext bc;

	private ConcurrentMap<String, CsvHttpService> serviceMap;
	/*
	 * key: service name, value: CsvMethod list
	 */
	private ConcurrentMap<String, Set<String>> methodMap;

	public CsvHttpServiceApiImpl(BundleContext bc) {
		super(bc, CsvHttpService.class.getName(), null);
		this.bc = bc;
	}

	@Validate
	protected void start() {
		serviceMap = new ConcurrentHashMap<String, CsvHttpService>();
		methodMap = new ConcurrentHashMap<String, Set<String>>();

		try {
			ServiceReference[] refs = bc.getServiceReferences(CsvHttpService.class.getName(), null);
			if (refs != null) {
				for (ServiceReference ref : refs) {
					CsvHttpService service = (CsvHttpService) bc.getService(ref);
					if (service == null)
						continue;

					addCsvMapping(service);
				}
			}
		} catch (InvalidSyntaxException e) {
		}

		open();
	}

	@Invalidate
	protected void stop() {
		close();

		serviceMap.clear();
		methodMap.clear();
	}

	@Override
	public Map<String, Set<String>> getAvailableFilters() {
		return methodMap;
	}

	@Override
	public Object invokeCsvMethod(String filterId, String methodName, HttpServletRequest req) throws Exception {
		Set<String> csvMethods = methodMap.get(filterId);
		if (csvMethods == null)
			throw new NoSuchMethodException();

		if (!csvMethods.contains(methodName))
			throw new NoSuchMethodException();

		CsvHttpService service = serviceMap.get(filterId);
		if (service == null)
			throw new NoSuchMethodException();

		logger.debug("kraken csv servlet: calling csv method: {}", methodName);
		Method method = service.getClass().getMethod(methodName, new Class[] { HttpServletRequest.class });
		return method.invoke(service, new Object[] { req });
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CsvRow> invokeCsvMethod(String filterId, String methodName, Map<String, Object> params)
			throws Exception {
		Set<String> csvMethods = methodMap.get(filterId);
		if (csvMethods == null)
			throw new NoSuchMethodException();

		if (!csvMethods.contains(methodName))
			throw new NoSuchMethodException();

		CsvHttpService service = serviceMap.get(filterId);
		if (service == null)
			throw new NoSuchMethodException();

		logger.debug("[csv-servlet] calling csv method: {}", methodName);
		Method method = service.getClass().getMethod(methodName, new Class[] { Map.class });
		return (List<CsvRow>) method.invoke(service, new Object[] { params });
	}

	@Override
	public void registerServlet(String serverId, String pathSpec) throws ServletException, NamespaceException {
		KrakenHttpService server = httpServiceManager.getHttpService(serverId);
		server.registerServlet(pathSpec, new CsvHttpServlet(this), null, null);
	}

	private void addCsvMapping(CsvHttpService service) {
		Set<String> csvMethods = new HashSet<String>();
		Method[] methods = service.getClass().getMethods();
		for (Method method : methods) {
			CsvHttpMethod m = method.getAnnotation(CsvHttpMethod.class);
			if (m != null) {
				logger.trace("[csv] registering filter method: " + method.getName());
				csvMethods.add(method.getName());
			}
		}

		if (csvMethods.size() > 0) {
			serviceMap.put(service.getName(), service);
			methodMap.put(service.getName(), csvMethods);
		}
	}

	private void removeCsvMapping(CsvHttpService service) {
		methodMap.remove(service.getName());
	}

	@Override
	public Object addingService(ServiceReference reference) {
		CsvHttpService csvHttp = (CsvHttpService) super.addingService(reference);
		logger.trace("[csv-servlet] filter loaded: {}", csvHttp.getName());
		addCsvMapping(csvHttp);
		return csvHttp;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		CsvHttpService csvHttp = (CsvHttpService) service;
		logger.trace("[csv-servlet] filter loaded: {}", csvHttp.getName());
		removeCsvMapping(csvHttp);
		super.removedService(reference, service);
	}
}
