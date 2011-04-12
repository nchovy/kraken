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
package org.krakenapps.pcap.script;

import java.io.IOException;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.pcap.live.PcapDevice;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.krakenapps.pcap.live.PcapStreamManager;
import org.krakenapps.pcap.live.PcapStat;
import org.krakenapps.pcap.live.Promiscuous;
import org.krakenapps.pcap.util.PcapLiveRunner;

/**
 * @author xeraph
 */
public class PcapScript implements Script {
	private PcapStreamManager streamManager;
	private ScriptContext context;

	public PcapScript(PcapStreamManager streamManager) {
		this.streamManager = streamManager;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void ping(String[] args) {
	}

	public void streams(String[] args) {
		context.println("Live Streams");
		context.println("-----------------------");
		for (String key : streamManager.getStreamKeys()) {
			PcapLiveRunner runner = streamManager.get(key);
			PcapDevice device = runner.getDevice();
			PcapStat stat = streamManager.getStat(key);
			PcapDeviceMetadata metadata = device.getMetadata();
			context.printf("%s: %s %s\n", key, metadata.getDescription(), stat.toString());
		}
	}

	public void devices(String[] args) {
		context.println("Available Devices");
		context.println("-----------------------");

		int i = 1;
		for (PcapDeviceMetadata metadata : PcapDeviceManager.getDeviceMetadataList()) {
			context.printf("[%2d] %s %s\n", i++, metadata.getName(), metadata.getDescription());
		}
	}

	@ScriptUsage(description = "print pcap device stats", arguments = { @ScriptArgument(name = "alias", type = "string", description = "the alias of the pcap device") })
	public void stats(String[] args) {
		String alias = args[0];
		PcapStat stat = streamManager.getStat(alias);
		if (stat == null) {
			context.println("device not found");
			return;
		}

		context.println(stat.toString());
	}

	@ScriptUsage(description = "open and register pcap device", arguments = {
			@ScriptArgument(name = "alias", type = "string", description = "alias of the pcap device"),
			@ScriptArgument(name = "device index", type = "int", description = "index of the pcap device"),
			@ScriptArgument(name = "timeout", type = "int", description = "milliseconds"),
			@ScriptArgument(name = "promiscuous mode", type = "string", description = "promisc or nonpromisc", optional = true),
			@ScriptArgument(name = "bpf", type = "string", description = "bpf filter expression", optional = true) })
	public void open(String[] args) {
		try {
			String alias = args[0];
			int select = Integer.parseInt(args[1]);
			int milliseconds = Integer.parseInt(args[2]);
			Promiscuous promisc = null;
			String filter = null;

			if (args.length > 4)
				filter = args[4];
			if (args.length > 3)
				promisc = args[3].equals("promisc") ? Promiscuous.On : Promiscuous.Off;

			String deviceName = null;
			int i = 1;
			for (PcapDeviceMetadata metadata : PcapDeviceManager.getDeviceMetadataList()) {
				if (i == select) {
					deviceName = metadata.getName();
					break;
				}

				i++;
			}

			streamManager.start(alias, deviceName, milliseconds, promisc, filter);
			context.println("stream opened");
		} catch (IOException e) {
			context.println("open failed");
		}
	}

	@ScriptUsage(description = "close and unregister the pcap device", arguments = { @ScriptArgument(name = "alias", type = "string", description = "alias of the pcap device") })
	public void close(String[] args) {
		String alias = args[0];
		if (streamManager.get(alias) == null) {
			context.println("stream not found");
			return;
		}

		streamManager.stop(alias);
		context.println("stopped");
	}
}
