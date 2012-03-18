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
package org.krakenapps.winapi;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.winapi.TcpConnectionInformation.State;

public class IpGlobalProperties {
	static {
		System.loadLibrary("winapi");
	}

	public static enum Protocol {
		IPv4, IPv6
	};

	private static native TcpConnectionInformation[] getTcpConnections(boolean isIpv4);

	public static TcpConnectionInformation[] getTcpConnections(Protocol protocol) {
		if (protocol == Protocol.IPv4)
			return getTcpConnections(true);
		else
			return getTcpConnections(false);
	}

	public static TcpConnectionInformation[] getAllTcpConnections() {
		TcpConnectionInformation[] tcp4Stat = getTcpConnections(Protocol.IPv4);
		TcpConnectionInformation[] tcp6Stat = getTcpConnections(Protocol.IPv6);
		TcpConnectionInformation[] tcpStat = new TcpConnectionInformation[tcp4Stat.length + tcp6Stat.length];

		for (int i = 0; i < tcp4Stat.length; i++)
			tcpStat[i] = tcp4Stat[i];
		for (int i = tcp4Stat.length; i < tcpStat.length; i++)
			tcpStat[i] = tcp6Stat[i - tcp4Stat.length];

		return tcpStat;
	}

	public static TcpConnectionInformation[] getActiveTcpConnections() {
		List<TcpConnectionInformation> stats = new ArrayList<TcpConnectionInformation>();
		TcpConnectionInformation[] all = getAllTcpConnections();

		for (TcpConnectionInformation obj : all) {
			if (obj.getState() != State.Closed && obj.getState() != State.Listen)
				stats.add(obj);
		}

		return (TcpConnectionInformation[]) stats.toArray();
	}

	private static native UdpListenerInformation[] getUdpListeners(boolean isIpv4);

	public static UdpListenerInformation[] getUdpListeners(Protocol protocol) {
		if (protocol == Protocol.IPv4)
			return getUdpListeners(true);
		else
			return getUdpListeners(false);
	}

	public static UdpListenerInformation[] getAllUdpListeners() {
		UdpListenerInformation[] udp4Stat = getUdpListeners(Protocol.IPv4);
		UdpListenerInformation[] udp6Stat = getUdpListeners(Protocol.IPv6);
		UdpListenerInformation[] udpStat = new UdpListenerInformation[udp4Stat.length + udp6Stat.length];

		for (int i = 0; i < udp4Stat.length; i++)
			udpStat[i] = udp4Stat[i];
		for (int i = udp4Stat.length; i < udpStat.length; i++)
			udpStat[i] = udp6Stat[i - udp4Stat.length];

		return udpStat;
	}

}
