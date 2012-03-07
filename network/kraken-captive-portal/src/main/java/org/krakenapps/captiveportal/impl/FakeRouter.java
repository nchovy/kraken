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

import org.krakenapps.captiveportal.CaptivePortal;
import org.krakenapps.pcap.decoder.ethernet.EthernetDecoder;
import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.decoder.ethernet.EthernetProcessor;
import org.krakenapps.pcap.decoder.ethernet.EthernetType;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.live.PcapDevice;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.packet.PcapPacket;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.IpConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeRouter implements Runnable, EthernetProcessor {
	private final Logger logger = LoggerFactory.getLogger(FakeRouter.class.getName());

	private Thread t;
	private PcapDevice device;
	private volatile boolean doStop;

	private MacAddress localmac;

	private CaptivePortal portal;

	public FakeRouter(String deviceName, CaptivePortal portal) throws IOException {
		this.device = PcapDeviceManager.open(deviceName, 1000000);
		this.localmac = device.getMetadata().getMacAddress();
		this.portal = portal;
	}

	public void start() {
		t = new Thread(this, "Captive Portal Fake Router");
		t.start();
	}

	public void stop() {
		try {
			doStop = true;
			t.interrupt();
			device.close();
		} catch (IOException e) {
			logger.error("kraken captive portal: cannot close device", e);
		}
	}

	@Override
	public void run() {
		EthernetDecoder eth = new EthernetDecoder();
		eth.register(EthernetType.IPV4, this);

		doStop = false;

		try {
			while (!doStop) {
				PcapPacket packet = device.getPacket();
				eth.decode(packet);
			}
		} catch (IOException e) {
			logger.error("kraken captive portal: routing io error", e);
		} finally {
			logger.info("kraken captive portal: fake router stopped");
		}
	}

	@Override
	public void process(EthernetFrame frame) {
		Buffer buf = frame.getData();
		buf.skip(9);
		int protocol = buf.get();
		buf.skip(2);

		InetAddress src = IpConverter.toInetAddress(buf.getInt());
		InetAddress dst = IpConverter.toInetAddress(buf.getInt());
		int srcPort = buf.getUnsignedShort();
		int dstPort = buf.getUnsignedShort();

		if (dst.isMulticastAddress())
			return;

		buf.rewind();
		byte[] b = new byte[buf.readableBytes() + 14];
		buf.gets(b, 14, b.length - 14);
		b[12] = 0x08;
		b[13] = 0x00;

		MacAddress srcmac = frame.getSource();
		MacAddress dstmac = frame.getDestination();
		MacAddress qsrcmac = portal.getQuarantinedMac(src);
		MacAddress qdstmac = portal.getQuarantinedMac(dst);
		MacAddress gwmac = portal.getGatewayMacAddress();
		if (gwmac == null)
			return;

		if (srcmac.equals(localmac))
			return;

		// packet from target host
		if (qsrcmac != null && !srcmac.equals(gwmac)) {
			if (FakeDns.isDnsPacket(protocol, dstPort)) {
				FakeDns.forgeResponse(portal.getRedirectAddress(), device, frame, src, srcPort, dst, dstPort, buf);
				return;
			}

			// replace src mac -> local mac, dst mac -> real gateway mac, and
			// forward it
			byte[] d = gwmac.getBytes();
			for (int i = 0; i < 6; i++)
				b[i] = d[i];

			byte[] s = localmac.getBytes();
			for (int i = 0; i < 6; i++)
				b[i + 6] = s[i];

			sendPacket(b);
			return;
		}

		// packet to target host
		if (qdstmac != null && !dstmac.equals(gwmac)) {
			if (FakeDns.isDnsPacket(protocol, dstPort)) {
				FakeDns.forgeResponse(portal.getRedirectAddress(), device, frame, src, srcPort, dst, dstPort, buf);
				return;
			}

			// replace src mac -> local mac, dst mac -> real host mac, and
			// forward it
			byte[] d = qdstmac.getBytes();
			for (int i = 0; i < 6; i++)
				b[i] = d[i];

			byte[] s = localmac.getBytes();
			for (int i = 0; i < 6; i++)
				b[i + 6] = s[i];

			sendPacket(b);
			return;
		}
	}

	private void sendPacket(byte[] b) {
		try {
			device.write(b);
		} catch (IOException e) {
			logger.error("kraken captive portal: cannot route packet", e);
		}
	}
}
