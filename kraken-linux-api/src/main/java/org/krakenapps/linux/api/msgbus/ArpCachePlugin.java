package org.krakenapps.linux.api.msgbus;

import org.apache.felix.ipojo.annotations.Component;
import org.krakenapps.linux.api.ArpCache;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "linux-arp-cache-plugin")
@MsgbusPlugin
public class ArpCachePlugin {
	@MsgbusMethod
	public void getEntries(Request req, Response resp) { 
		resp.put("arp_cache", Marshaler.marshal(ArpCache.getEntries()));
	}
}