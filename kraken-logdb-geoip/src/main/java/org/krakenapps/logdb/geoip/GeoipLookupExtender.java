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
		if (value instanceof InetAddress)
			return geoip.locate((InetAddress) value);

		if (value instanceof String) {
			try {
				InetAddress ip = InetAddress.getByName((String) value);
				GeoIpLocation location = geoip.locate(ip);
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
			} catch (Throwable t) {
				return null;
			}
		}

		return null;
	}
}
