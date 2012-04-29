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
package org.krakenapps.sentry.linux.logger;

import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.krakenapps.linux.api.ArpCache;
import org.krakenapps.linux.api.ArpEntry;
import org.krakenapps.linux.api.ConnectionInformation;
import org.krakenapps.linux.api.Process;
import org.krakenapps.linux.api.TcpConnectionInformation;
import org.krakenapps.linux.api.UdpConnectionInformation;

@Component(name = "sentry-linux-command-handler")
@Provides
public class LinuxCommandHandler implements SentryCommandHandler {
	@Override
	public Collection<String> getFeatures() {
		return Arrays.asList("process-list", "arp-cache", "netstat");
	}

	@SentryMethod
	public List<Object> getProcesses() {
		List<Object> list = new ArrayList<Object>();

		for (Process p : Process.getProcesses()) {
			Map<String, Object> entryMap = new HashMap<String, Object>();

			entryMap.put("pid", p.getPid());
			entryMap.put("name", p.getName());
			entryMap.put("cpu_usage", 0);
			entryMap.put("working_set", 0);

			list.add(entryMap);
		}

		return list;
	}

	@SentryMethod
	public List<Object> getArpCache() throws FileNotFoundException {
		List<Object> list = new ArrayList<Object>();

		for (ArpEntry entry : ArpCache.getEntries()) {
			Map<String, Object> entryMap = new HashMap<String, Object>();

			entryMap.put("adapter", entry.getDevice());
			entryMap.put("type", entry.getFlags().toLowerCase());
			entryMap.put("mac", entry.getMac());
			entryMap.put("ip", entry.getIp());

			list.add(entryMap);
		}

		return list;
	}

	@SentryMethod
	public Map<String, Object> getNetStat() {
		Map<String, Object> m = new HashMap<String, Object>();

		try {
			m.put("tcp", marshal(TcpConnectionInformation.getTcpInformations()));
			m.put("tcp6", marshal(TcpConnectionInformation.getTcp6Informations()));
			m.put("udp", marshal(UdpConnectionInformation.getUdpInformations()));
			m.put("udp6", marshal(UdpConnectionInformation.getUdp6Informations()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return m;
	}

	private List<Object> marshal(List<ConnectionInformation> infos) {
		List<Object> list = new ArrayList<Object>();

		for (ConnectionInformation info : infos) {
			Map<String, Object> infoMap = new HashMap<String, Object>();

			infoMap.put("local_ip", info.getLocal().getAddress().getHostAddress());
			infoMap.put("local_port", info.getLocal().getPort());
			if (info instanceof TcpConnectionInformation) {
				infoMap.put("remote_ip", info.getRemote().getAddress().getHostAddress());
				infoMap.put("remote_port", info.getRemote().getPort());
			}
			infoMap.put("state", info.getState().toString().toUpperCase());
			infoMap.put("pid", info.getPid());

			list.add(infoMap);
		}

		return list;
	}
}
