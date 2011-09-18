/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	public void getEthernetInterfaces(Request req, Response resp) throws IOException {
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
	public void setEthernetInterfaces(Request req, Response resp) throws IOException {
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
