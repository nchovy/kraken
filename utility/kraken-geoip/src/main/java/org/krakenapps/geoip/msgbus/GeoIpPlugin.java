package org.krakenapps.geoip.msgbus;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.geoip.GeoIpLocation;
import org.krakenapps.geoip.GeoIpService;
import org.krakenapps.msgbus.MsgbusException;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@MsgbusPlugin
@Component(name = "geoip-plugin")
public class GeoIpPlugin {

	@Requires
	private GeoIpService geoip;

	@MsgbusMethod
	public void locate(Request req, Response resp) {
		InetAddress address;
		try {
			address = InetAddress.getByName(req.getString("ip"));
		} catch (UnknownHostException e) {
			throw new MsgbusException("geoip", "invalid-ip");
		}

		GeoIpLocation location = geoip.locate(address);
		resp.put("id", location.getId());
		resp.put("city", location.getCity());
		resp.put("country", location.getCountry());
		resp.put("latitude", location.getLatitude());
		resp.put("longitude", location.getLongitude());
		resp.put("region", location.getRegion());
		resp.put("metro_code", location.getMetroCode());
		resp.put("area_code", location.getAreaCode());
		resp.put("postal_code", location.getPostalCode());
	}
}
