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
package org.krakenapps.arpwatch.impl;

import java.net.InetAddress;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.arpwatch.ArpCache;
import org.krakenapps.arpwatch.ArpEntry;
import org.krakenapps.arpwatch.ArpStaticBinding;
import org.krakenapps.arpwatch.ArpStaticBindingConfig;
import org.krakenapps.arpwatch.ArpWatcher;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;

public class ArpWatcherScript implements Script {
	private ArpWatcher watcher;
	private ScriptContext context;

	public ArpWatcherScript(ArpWatcher watcher) {
		this.watcher = watcher;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "all connected live streams")
	public void streams(String[] args) {
		context.println("Connected Streams");
		context.println("-----------------------");
		for (String key : watcher.getStreamKeys()) {
			context.println(key);
		}
	}

	@ScriptUsage(description = "connect to live pcap stream", arguments = { @ScriptArgument(name = "name", type = "string", description = "the name of live stream") })
	public void connect(String[] args) {
		try {
			watcher.start(args[0]);
			context.println("stream connected");
		} catch (Exception e) {
			context.println("connect failed: " + e.getMessage());
		}
	}

	@ScriptUsage(description = "stop arp watcher")
	public void stop(String[] args) {
		watcher.stop();
	}

	public void entries(String[] args) {
		ArpCache cache = watcher.getArpCache();

		context.println("Cached Entries");
		context.println("-----------------------");

		for (ArpEntry entry : cache.getCachedEntries()) {
			context.println(entry.toString());
		}
	}

	public void bindings(String[] args) {
		ArpStaticBindingConfig config = watcher.getStaticBindingConfig();

		context.println("Static Bindings");
		context.println("-----------------------");
		for (ArpStaticBinding binding : config.getStaticBindings()) {
			context.println(binding.toString());
		}
	}

	public void addBinding(String[] args) {
		ArpStaticBindingConfig config = watcher.getStaticBindingConfig();
		try {
			InetAddress ip = InetAddress.getByName(args[0]);
			MacAddress mac = new MacAddress(args[1]);

			config.addStaticBinding(new ArpStaticBindingImpl(mac, ip));
		} catch (Exception e) {
			context.println("static binding failed: " + e.getMessage());
		}
	}

	public void removeBinding(String[] args) {
		ArpStaticBindingConfig config = watcher.getStaticBindingConfig();
		try {
			InetAddress ip = InetAddress.getByName(args[0]);
			config.removeStaticBinding(ip);
		} catch (Exception e) {
			context.println("static unbindg failed: " + e.getMessage());
		}
	}
}
