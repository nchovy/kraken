/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.dns.script;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.dns.DnsCache;
import org.krakenapps.dns.DnsCacheEntry;
import org.krakenapps.dns.DnsCacheKey;
import org.krakenapps.dns.DnsMessage;
import org.krakenapps.dns.DnsResolverProvider;
import org.krakenapps.dns.ProxyResolverProvider;
import org.krakenapps.dns.DnsResourceRecord.Clazz;
import org.krakenapps.dns.DnsResourceRecord.Type;
import org.krakenapps.dns.DnsService;
import org.krakenapps.dns.EmptyDnsMessageListener;
import org.krakenapps.dns.rr.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xeraph@nchovy.com
 */

public class DnsScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(DnsScript.class);
	private ScriptContext context;
	private DnsService dns;
	private ProxyResolverProvider proxy;

	public DnsScript(DnsService dns, ProxyResolverProvider proxy) {
		this.dns = dns;
		this.proxy = proxy;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void status(String[] args) {
		context.println(dns.getStatus());
	}

	public void open(String[] args) {
		try {
			dns.open();
			context.println("port opened");
		} catch (IOException e) {
			context.println(e.getMessage());
			logger.error("kraken dns: cannot open dns server", e);
		}
	}

	public void close(String[] args) {
		dns.close();
		context.println("port closed");
	}

	public void cacheEntries(String[] args) {
		context.println("Cached DNS Responses");
		context.println("----------------------");
		DnsCache cache = dns.getCache();
		for (DnsCacheKey key : cache.getKeys()) {
			DnsCacheEntry entry = cache.lookup(key);
			context.println(key + " => " + entry);
		}
	}

	@ScriptUsage(description = "add cache entry (no timeout)", arguments = {
			@ScriptArgument(name = "domain", type = "string", description = "domain name"),
			@ScriptArgument(name = "ipv4 address", type = "string", description = "mapped ip address") })
	public void addCacheEntry(String[] args) throws UnknownHostException {
		String domain = args[0];
		InetAddress addr = InetAddress.getByName(args[1]);

		DnsCache cache = dns.getCache();
		DnsMessage response = new DnsMessage();
		response.addAnswer(new A(domain, addr, 0));

		cache.putEntry(new DnsCacheKey(domain, Type.A, Clazz.IN), new DnsCacheEntry(response, 0));
		context.println("added");
	}

	@ScriptUsage(description = "remove cache entry", arguments = { @ScriptArgument(name = "domain", type = "string", description = "domain name") })
	public void removeCacheEntry(String[] args) {
		String domain = args[0];

		DnsCache cache = dns.getCache();
		cache.removeEntry(new DnsCacheKey(domain, Type.A, Clazz.IN));
		context.println("removed");
	}

	public void trace(String[] args) {
		DnsEventPrinter printer = new DnsEventPrinter();
		dns.addListener(printer);

		try {
			context.println("tracing dns events...");
			while (true) {
				context.readLine();
			}
		} catch (InterruptedException e) {
			context.println("");
			context.println("interrupted");
		} finally {
			dns.removeListener(printer);
			context.println("stopped");
		}
	}

	public void providers(String[] args) {
		context.println("Resolver Providers");
		context.println("--------------------");

		for (DnsResolverProvider provider : dns.getResolverProviders()) {
			context.println(provider.getName() + ": " + provider);
		}
	}

	public void proxyNameServer(String[] args) {
		InetAddress addr;
		try {
			if (args.length > 0) {
				addr = InetAddress.getByName(args[0]);
				proxy.setNameServer(addr);
				context.println("set");
			} else {
				context.println(proxy.getNameServer().getHostAddress());
			}
		} catch (UnknownHostException e) {
			context.println("invalid nameserver address");
		}
	}

	private class DnsEventPrinter extends EmptyDnsMessageListener {
		private String getRemoteAddress(DatagramPacket packet) {
			return packet.getAddress().getHostAddress() + ":" + packet.getPort();
		}

		@Override
		public void onReceive(DatagramPacket queryPacket, DnsMessage query) {
			context.println("[RECV " + getRemoteAddress(queryPacket) + "] " + query);
		}

		@Override
		public void onSend(DatagramPacket queryPacket, DnsMessage query, DatagramPacket responsePacket, DnsMessage response) {
			context.println("[SENT " + getRemoteAddress(responsePacket) + "] " + response);
		}

		@Override
		public void onError(DatagramPacket packet, Throwable t) {
			context.println("[ERROR " + getRemoteAddress(packet) + "] "
					+ toHexDump(packet.getData(), packet.getOffset(), packet.getLength()));
		}

		@Override
		public void onDrop(DatagramPacket queryPacket, DnsMessage query, Throwable t) {
			context.println("[DROP " + getRemoteAddress(queryPacket) + "] " + query);
		}

		private String toHexDump(byte[] buf, int offset, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < length; i++) {
				if (i != 0)
					sb.append(" ");
				sb.append(String.format("%02X", buf[i + offset]));
			}

			return sb.toString();
		}
	}
}
