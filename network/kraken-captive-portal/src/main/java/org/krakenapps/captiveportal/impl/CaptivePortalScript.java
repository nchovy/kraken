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
package org.krakenapps.captiveportal.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.captiveportal.CaptivePortal;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaptivePortalScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(CaptivePortalScript.class.getName());

	private CaptivePortal portal;
	private ScriptContext context;

	public CaptivePortalScript(CaptivePortal portal) {
		this.portal = portal;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void redirectip(String[] args) {
		InetAddress ip = portal.getRedirectAddress();
		if (ip != null)
			context.println(ip);
		else
			context.println("not set");
	}

	@ScriptUsage(description = "set redirect ip address", arguments = { @ScriptArgument(name = "redirect ip", type = "string", description = "redirect ip address") })
	public void setredirectip(String[] args) {
		try {
			portal.setRedirectAddress(InetAddress.getByName(args[0]));
			context.println("set");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken captive portal: cannot set redirect ip", e);
		}
	}

	public void arpcache(String[] args) {
		context.println("ARP Cache");
		context.println("-------------");

		Map<InetAddress, MacAddress> m = portal.getArpCache();
		for (InetAddress ip : m.keySet()) {
			context.println("ip=" + ip.getHostAddress() + ", mac=" + m.get(ip));
		}
	}

	@ScriptUsage(description = "set poison interval", arguments = { @ScriptArgument(name = "interval", type = "int", description = "poision interval in milliseconds") })
	public void setpoisoninterval(String[] args) {
		try {
			portal.setPoisonInterval(Integer.valueOf(args[0]));
			context.println("set");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "get poison interval")
	public void poisoninterval(String[] args) {
		context.println(portal.getPoisonInterval() + "ms");
	}

	@ScriptUsage(description = "set gateway ip", arguments = { @ScriptArgument(name = "gateway ip", type = "string", description = "gateway ip address") })
	public void setgateway(String[] args) {
		try {
			InetAddress ip = InetAddress.getByName(args[0]);
			portal.setGatewayAddress(ip);
			context.println("set");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "get gateway ip")
	public void gateway(String[] args) {
		context.println(portal.getGatewayAddress());
	}

	@ScriptUsage(description = "set pcap device name")
	public void setdevice(String[] args) {
		try {
			int i = 1;
			List<PcapDeviceMetadata> metadatas = PcapDeviceManager.getDeviceMetadataList();
			for (PcapDeviceMetadata metadata : metadatas) {
				context.println("[" + (i++) + "] " + metadata);
			}

			context.print("select? ");
			String line = context.readLine();
			int num = Integer.valueOf(line);

			if (num < 0 || num > metadatas.size()) {
				context.println("invalid number");
				return;
			}

			PcapDeviceMetadata selected = metadatas.get(num - 1);
			portal.setPcapDeviceName(selected.getName());
			context.println("set");
		} catch (NumberFormatException e) {
			context.println("invalid number");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "get pcap device name")
	public void device(String[] args) {
		String name = portal.getPcapDeviceName();
		if (name == null)
			context.println("not set");
		else
			context.println(name);
	}

	@ScriptUsage(description = "set mirroring mode", arguments = { @ScriptArgument(name = "mirroring mode", type = "string", description = "true or false") })
	public void setmirroring(String[] args) {
		try {
			portal.setMirroringMode(Boolean.parseBoolean(args[0]));
			context.println("set");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "get mirroring mode")
	public void mirroring(String[] args) {
		context.println(portal.getMirroringMode());
	}

	public void targets(String[] args) {
		context.println("Quarantined Hosts");
		context.println("-------------------");
		for (InetAddress target : portal.getQuarantinedHosts()) {
			context.println(target.getHostAddress());
		}
	}

	@ScriptUsage(description = "quarantine new host ip", arguments = { @ScriptArgument(name = "target ip address") })
	public void quarantine(String[] args) {
		try {
			InetAddress address = InetAddress.getByName(args[0]);
			portal.quarantineHost(address);
			context.println("set");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken captive portal: cannot quarantine host " + args[0], e);
		}
	}

	@ScriptUsage(description = "unquarantine host ip", arguments = { @ScriptArgument(name = "target ip address") })
	public void unquarantine(String[] args) {
		try {
			InetAddress address = InetAddress.getByName(args[0]);
			portal.unquarantineHost(address);
			context.println("unset");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken captive portal: cannot unquarantine host " + args[0], e);
		}
	}

	public void spoof(String[] args) {
		try {
			portal.spoof();
		} catch (IOException e) {
			context.println(e.getMessage());
		}
	}
}
