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
package org.krakenapps.pcap.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.krakenapps.pcap.decoder.ip.Ipv4Packet;
import org.krakenapps.pcap.decoder.tcp.TcpSegment;
import org.krakenapps.pcap.decoder.tcp.TcpSegmentCallback;
import org.krakenapps.pcap.decoder.tcp.TcpSession;
import org.krakenapps.pcap.live.PcapDevice;
import org.krakenapps.pcap.live.PcapDeviceManager;
import static org.krakenapps.pcap.util.PacketManipulator.*;

public class TcpPortScanner {
	private TcpPortScanner() {
	}

	public static TcpSegment finScan(InetSocketAddress target, int timeout) throws IOException {
		return sendAndReceive(IP().data(TCP().fin().dst(target)), timeout);
	}

	public static TcpSegment synScan(InetSocketAddress target, int timeout) throws IOException {
		return sendAndReceive(IP().data(TCP().syn().dst(target)), timeout);
	}

	public static TcpSegment xmasScan(InetSocketAddress target, int timeout) throws IOException {
		return sendAndReceive(IP().data(TCP().fin().urg().psh().dst(target)), timeout);
	}

	public static TcpSegment nullScan(InetSocketAddress target, int timeout) throws IOException {
		return sendAndReceive(IP().data(TCP().dst(target)), timeout);
	}

	private static TcpSegment sendAndReceive(Ipv4Packet.Builder ip, int timeout) throws IOException {
		InetAddress target = (InetAddress) ip.getDefault("dst_ip");
		int targetPort = (Integer) ip.getDefault("dst_port");

		PcapDevice device = PcapDeviceManager.openFor(target, timeout);
		try {
			send(ip);
			return receive(device, new InetSocketAddress(target, targetPort), timeout);
		} catch (TimeoutException e) {
		}

		return null;
	}

	private static TcpSegment receive(PcapDevice device, InetSocketAddress target, int timeout) throws IOException,
			TimeoutException {
		LastPacketCallback callback = new LastPacketCallback();

		PcapLiveRunner runner = new PcapLiveRunner(device);
		runner.getTcpDecoder().registerSegmentCallback(callback);

		long begin = new Date().getTime();
		while (true) {
			runner.runOnce();
			TcpSegment last = callback.lastTcpSegment;

			if (last != null && last.getSource().equals(target))
				return last;

			long end = new Date().getTime();

			if (end - begin > timeout)
				throw new TimeoutException("tcp response not found");
		}

	}

	private static class LastPacketCallback implements TcpSegmentCallback {
		private TcpSegment lastTcpSegment;

		@Override
		public void onReceive(TcpSession session, TcpSegment segment) {
			this.lastTcpSegment = segment;
		}
	}

}
