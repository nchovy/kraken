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

import org.krakenapps.http.KrakenHttpService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class HttpServiceTracker extends ServiceTracker {
	private HttpServiceController controller;

	public HttpServiceTracker(BundleContext context, HttpServiceController controller) {
		super(context, KrakenHttpService.class.getName(), null);
		this.controller = controller;
	}

	@Override
	public Object addingService(ServiceReference reference) {
		System.out.println("new kraken http service detected.");
		long serviceId = getServiceId(reference);

		KrakenHttpService httpService = (KrakenHttpService) super.addingService(reference);
		controller.registerHttpService(serviceId, httpService);
		return httpService;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		long serviceId = getServiceId(reference);
		controller.unregisterHttpService(serviceId);
		super.removedService(reference, service);
	}

	private long getServiceId(ServiceReference reference) {
		long serviceId = (Long) reference.getProperty(Constants.SERVICE_ID);
		return serviceId;
	}

}
