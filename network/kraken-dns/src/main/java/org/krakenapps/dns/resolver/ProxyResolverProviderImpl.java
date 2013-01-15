/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.dns.resolver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.dns.DnsDump;
import org.krakenapps.dns.DnsMessage;
import org.krakenapps.dns.DnsMessageCodec;
import org.krakenapps.dns.DnsResolver;
import org.krakenapps.dns.DnsService;
import org.krakenapps.dns.ProxyResolverProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "dns-proxy-resolver-provider")
@Provides
public class ProxyResolverProviderImpl implements ProxyResolverProvider {
	private final Logger logger = LoggerFactory.getLogger(ProxyResolverProviderImpl.class);

	@Requires
	private DnsService dns;

	private InetAddress dnsAddr;

	public ProxyResolverProviderImpl() {
		try {
			dnsAddr = InetAddress.getByName("8.8.8.8");
		} catch (UnknownHostException e) {
		}
	}

	@Validate
	public void start() {
		dns.registerProvider(this);
	}

	@Invalidate
	public void stop() {
		if (dns != null)
			dns.unregisterProvider(this);
	}

	@Override
	public String getName() {
		return "proxy";
	}

	@Override
	public DnsResolver newResolver() {
		return new ProxyResolver();
	}

	@Override
	public InetAddress getNameServer() {
		return dnsAddr;
	}

	@Override
	public void setNameServer(InetAddress addr) {
		dnsAddr = addr;
	}

	private class ProxyResolver implements DnsResolver {

		@Override
		public DnsMessage resolve(DnsMessage query) throws IOException {
			byte[] rx = new byte[65536];
			DatagramPacket replyPacket = new DatagramPacket(rx, rx.length);
			DatagramSocket socket = null;
			try {
				socket = new DatagramSocket();
				socket.setSoTimeout(5000);

				ByteBuffer buf = DnsMessageCodec.encode(query);
				DatagramPacket queryPacket = new DatagramPacket(buf.array(), buf.limit());
				queryPacket.setAddress(dnsAddr);
				queryPacket.setPort(53);

				logger.debug("kraken dns: proxy sent query, {}", query);
				socket.send(queryPacket);

				socket.receive(replyPacket);

				DnsMessage response = DnsMessageCodec
						.decode(ByteBuffer.wrap(rx, replyPacket.getOffset(), replyPacket.getLength()));

				logger.debug("kraken dns: proxy received response, {}", response);
				return response;
			} catch (SocketTimeoutException e) {
				throw e;
			} catch (Throwable t) {
				logger.error("kraken dns: proxy response parse error - " + DnsDump.dumpPacket(replyPacket));
				throw new IllegalStateException("cannot parse nameserver response", t);
			} finally {
				if (socket != null)
					socket.close();
			}
		}
	}
}
