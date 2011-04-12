package org.krakenapps.linux.api.msgbus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.linux.api.EthernetInterface;
import org.krakenapps.linux.api.EthernetToolInformation;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "linux-ethernet-interface-plugin")
@Provides
@MsgbusPlugin
public class EthernetInterfacePlugin {

	@MsgbusMethod
	public void getEthernetInterfaces(Request req, Response resp) {
		List<Map<String, Object>> objs = new ArrayList<Map<String, Object>>();
		List<EthernetInterface> interfaces = EthernetInterface.getEthernetInterfaces();
		for (EthernetInterface ei : interfaces) {
			Map<String, Object> m = Marshaler.marshal(ei);
			try {
				EthernetToolInformation ethtoolInfo = EthernetToolInformation.getEthtoolInformation(ei.getDevice());
				m.put("ethtool_info", Marshaler.marshal(ethtoolInfo));
			} catch (IOException e) {
			}
			objs.add(m);
		}
		resp.put("interfaces", objs);
	}
}
