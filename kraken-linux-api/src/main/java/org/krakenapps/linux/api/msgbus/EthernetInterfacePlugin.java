package org.krakenapps.linux.api.msgbus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.krakenapps.linux.api.EthernetInterface;
import org.krakenapps.linux.api.EthernetToolInformation;
import org.krakenapps.msgbus.MsgbusException;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "linux-ethernet-interface-plugin")
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

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void setEthernetInterfaces(Request req, Response resp) {
		Map<String, Object> interfaces = (Map<String, Object>) req.get("interfaces");
		for (String devname : interfaces.keySet()) {
			try {
				Map<String, String> m = (Map<String, String>) interfaces.get(devname);
				EthernetInterface dev = new EthernetInterface(devname);
				InetAddress ipAddr = InetAddress.getByName(m.get("ip"));
				InetAddress netmask = InetAddress.getByName(m.get("netmask"));
				InetAddress gateway = InetAddress.getByName(m.get("gateway"));
				dev.setIpAddr(ipAddr);
				dev.setNetmask(netmask);
				dev.setGateway(gateway);
				dev.save();
			} catch (FileNotFoundException e) {
				throw new MsgbusException("0", "invalid device: " + devname);
			} catch (UnknownHostException e) {
				throw new MsgbusException("0", "unknown host: " + devname);
			}
		}
	}
}
