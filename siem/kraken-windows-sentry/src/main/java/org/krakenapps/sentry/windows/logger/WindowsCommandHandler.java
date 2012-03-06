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
package org.krakenapps.sentry.windows.logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.sentry.SentryCommandHandler;
import org.krakenapps.sentry.SentryMethod;
import org.krakenapps.winapi.ArpCache;
import org.krakenapps.winapi.ArpEntry;
import org.krakenapps.winapi.IpGlobalProperties;
import org.krakenapps.winapi.Process;
import org.krakenapps.winapi.RoutingEntry;
import org.krakenapps.winapi.RoutingTable;
import org.krakenapps.winapi.TcpConnectionInformation;
import org.krakenapps.winapi.UdpListenerInformation;
import org.krakenapps.winapi.IpGlobalProperties.Protocol;

@Component(name = "sentry-windows-command-handler")
@Provides
public class WindowsCommandHandler implements SentryCommandHandler {
	@Override
	public Collection<String> getFeatures() {
		return Arrays.asList("process-list", "arp-cache", "routing-table", "netstat");
	}

	@SentryMethod
	public List<Object> getProcesses() {
		List<Object> list = new ArrayList<Object>();
		Map<Integer, Integer> usages = null;
		try {
			usages = Process.getCpuUsages(100);
		} catch (InterruptedException e) {
		}

		for (Process p : Process.getProcesses()) {
			list.add(toMap(p, usages));
		}
		return list;
	}

	@SentryMethod
	public List<Object> getArpCache() {
		List<Object> l = new ArrayList<Object>();
		for (ArpEntry entry : ArpCache.getArpEntries()) {
			l.add(toMap(entry));
		}
		return l;
	}

	@SentryMethod
	public List<Object> getRoutingTable() {
		List<Object> l = new ArrayList<Object>();
		for (RoutingEntry entry : RoutingTable.getRoutingEntries()) {
			l.add(toMap(entry));
		}
		return l;
	}

	private Map<String, Object> toMap(RoutingEntry entry) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("type", entry.getType().toString());
		m.put("protocol", entry.getProtocol().toString());
		m.put("destination", entry.getDestination().getHostAddress());
		m.put("mask", entry.getSubnet().getHostAddress());
		m.put("forward", entry.getInterfaceAddress().getHostAddress());
		m.put("metric", entry.getMetric1());
		return m;
	}

	@SentryMethod
	public Map<String, Object> getNetStat() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("tcp", getTcpConnections());
		m.put("tcp6", getTcp6Connections());
		m.put("udp", getUdpListeners());
		m.put("udp6", getUdp6Listeners());
		return m;
	}

	private List<Object> getTcpConnections() {
		List<Object> l = new ArrayList<Object>();
		for (TcpConnectionInformation info : IpGlobalProperties.getTcpConnections(Protocol.IPv4)) {
			l.add(marshal(info));
		}
		return l;
	}

	private List<Object> getTcp6Connections() {
		List<Object> l = new ArrayList<Object>();
		for (TcpConnectionInformation info : IpGlobalProperties.getTcpConnections(Protocol.IPv6)) {
			l.add(marshal(info));
		}
		return l;
	}

	private List<Object> getUdpListeners() {
		List<Object> l = new ArrayList<Object>();
		for (UdpListenerInformation info : IpGlobalProperties.getUdpListeners(Protocol.IPv4)) {
			l.add(marshal(info));
		}
		return l;
	}

	private List<Object> getUdp6Listeners() {
		List<Object> l = new ArrayList<Object>();
		for (UdpListenerInformation info : IpGlobalProperties.getUdpListeners(Protocol.IPv6)) {
			l.add(marshal(info));
		}
		return l;
	}

	private Map<String, Object> marshal(TcpConnectionInformation info) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("local_ip", info.getLocal().getAddress().getHostAddress());
		m.put("local_port", info.getLocal().getPort());
		m.put("remote_ip", info.getRemote().getAddress().getHostAddress());
		m.put("remote_port", info.getRemote().getPort());
		m.put("state", info.getState().toString().toUpperCase());
		m.put("pid", info.getPid());
		return m;
	}

	private Map<String, Object> marshal(UdpListenerInformation info) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("local_ip", info.getLocal().getAddress().getHostAddress());
		m.put("local_port", info.getLocal().getPort());
		m.put("state", "LISTEN");
		m.put("pid", info.getPid());
		return m;
	}

	private Map<String, Object> toMap(ArpEntry entry) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("adapter", entry.getAdapterName());
		m.put("type", entry.getType().toString().toLowerCase());
		m.put("mac", getMacAddress(entry.getPhysicalAddress()));
		m.put("ip", entry.getAddress().getHostAddress());
		return m;
	}

	private String getMacAddress(byte[] b) {
		if (b != null && b.length == 6)
			return String.format("%02x:%02x:%02x:%02x:%02x:%02x", b[0], b[1], b[2], b[3], b[4], b[5]);
		return null;
	}

	private Map<String, Object> toMap(Process p, Map<Integer, Integer> usages) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("pid", p.getPid());
		m.put("name", p.getName());
		m.put("cpu_usage", usages.get(p.getPid()));
		m.put("working_set", p.getWorkingSet());
		return m;
	}
}
