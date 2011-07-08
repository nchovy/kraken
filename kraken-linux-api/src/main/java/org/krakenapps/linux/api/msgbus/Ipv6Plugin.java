package org.krakenapps.linux.api.msgbus;

import org.apache.felix.ipojo.annotations.Component;
import org.krakenapps.linux.api.Ipv6NeighborCache;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "linux-ipv6-neighbor-cache-plugin")
@MsgbusPlugin
public class Ipv6Plugin {
	@MsgbusMethod
	public void getNeighbors(Request req, Response resp) { 
		resp.put("neighbors", Marshaler.marshal(Ipv6NeighborCache.getEntries()));
	}
}
