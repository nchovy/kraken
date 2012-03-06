package org.krakenapps.http.internal;

import javax.servlet.Servlet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class HttpServletTracker extends ServiceTracker {
	HttpServletController controller;

	public HttpServletTracker(BundleContext bc, HttpServletController controller) throws InvalidSyntaxException {
		super(bc, Servlet.class.getName(), null);
		this.controller = controller;
	}

	@Override
	public Object addingService(ServiceReference reference) {
		System.out.printf("adding http-servlet instance - service-id: %d, instance.name: %s\n", (Long) reference
				.getProperty(Constants.SERVICE_ID), reference.getProperty("instance.name"));
		return super.addingService(reference);
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		System.out.printf("removing http-servlet instance - service-id: %d, instance.name: %s\n", (Long) reference
				.getProperty(Constants.SERVICE_ID), reference.getProperty("instance.name"));
		controller.unregisterServlet((String)reference.getProperty("instance.name"));
		super.removedService(reference, service);
	}

}
