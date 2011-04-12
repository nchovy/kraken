package org.krakenapps.util;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class BundleContextUtil {
	public static <T> T getInstanceFromBundleContext(BundleContext bc, Class<T> class1) {
		ServiceReference serviceReference = bc.getServiceReference(class1.getName());
		if (serviceReference == null) {
			//throw new NullPointerException(String.format("Service Reference is NULL (%s)", class1.getName()));
			return null;
		}
		try {
			T serviceInstance = class1.cast(bc.getService(serviceReference));
			return serviceInstance;
		} catch (ClassCastException cce) {
			return null;
		}
	}

}
