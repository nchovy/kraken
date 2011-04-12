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
