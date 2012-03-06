package org.krakenapps.logdb.geoip;

import java.net.InetAddress;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.geoip.GeoIpLocation;
import org.krakenapps.geoip.GeoIpService;
import org.krakenapps.logdb.LookupHandler;
import org.krakenapps.logdb.LookupHandlerRegistry;

@Component(name = "logdb-geoip")
public class GeoipLookupExtender implements LookupHandler {
	@Requires
	private GeoIpService geoip;

	@Requires
	private LookupHandlerRegistry lookup;

	@Validate
	public void start() {
		lookup.addLookupHandler("geoip", this);
	}

	@Invalidate
	public void stop() {
		if (lookup != null)
			lookup.removeLookupHandler("geoip");
	}

	@Override
	public Object lookup(String srcField, String dstField, Object value) {
		InetAddress ip = null;
		if (value instanceof InetAddress)
			ip = (InetAddress) value;

		if (value instanceof String) {
			try {
				ip = InetAddress.getByName((String) value);
			} catch (Throwable t) {
				return null;
			}
		}

		if (ip == null)
			return null;

		GeoIpLocation location = geoip.locate(ip);
		if (location == null)
			return null;

		if (dstField.equals("country"))
			return location.getCountry();
		else if (dstField.equals("region"))
			return location.getRegion();
		else if (dstField.equals("city"))
			return location.getCity();
		else if (dstField.equals("latitude"))
			return location.getLatitude();
		else if (dstField.equals("longitude"))
			return location.getLongitude();

		return null;
	}
}
