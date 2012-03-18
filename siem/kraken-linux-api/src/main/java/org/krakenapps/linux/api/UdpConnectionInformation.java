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
package org.krakenapps.linux.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UdpConnectionInformation extends ConnectionInformation {
	private UdpConnectionInformation() {
	}

	private InetSocketAddress local;
	private State state;
	private int pid;

	@Override
	public InetSocketAddress getLocal() {
		return local;
	}

	@Override
	public InetSocketAddress getRemote() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public int getPid() {
		return pid;
	}

	public static List<ConnectionInformation> getUdpInformations() throws IOException {
		Map<Integer, Integer> uidToPid = getUidToPidMap("UDP");
		return getTcpInformations("/proc/net/udp", uidToPid);
	}

	public static List<ConnectionInformation> getUdp6Informations() throws IOException {
		Map<Integer, Integer> uidToPid = getUidToPidMap("UDP");
		return getTcpInformations("/proc/net/udp6", uidToPid);
	}

	public static List<ConnectionInformation> getAllUdpInformations() throws IOException {
		List<ConnectionInformation> stats = new ArrayList<ConnectionInformation>();
		Map<Integer, Integer> uidToPid = getUidToPidMap("UDP");

		stats.addAll(getTcpInformations("/proc/net/udp", uidToPid));
		stats.addAll(getTcpInformations("/proc/net/udp6", uidToPid));

		return stats;
	}

	private static List<ConnectionInformation> getTcpInformations(String filePath, Map<Integer, Integer> uidToPid)
			throws IOException {
		List<ConnectionInformation> stats = new ArrayList<ConnectionInformation>();
		BufferedReader br = null;
		FileInputStream is = null;

		try {
			is = new FileInputStream(new File(filePath));
			br = new BufferedReader(new InputStreamReader(is));
			br.readLine(); // ignore column name line
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;

				if (filePath.endsWith("udp"))
					stats.add(parse(line, 4, uidToPid));
				else if (filePath.endsWith("udp6"))
					stats.add(parse(line, 16, uidToPid));
			}
		} finally {
			if (is != null)
				is.close();
			if (br != null)
				br.close();
		}

		return stats;
	}

	private static UdpConnectionInformation parse(String str, int addrSize, Map<Integer, Integer> uidToPid) {
		UdpConnectionInformation stat = new UdpConnectionInformation();

		String[] token = str.trim().replaceAll(" +", " ").split(" ");
		if (token.length < 12)
			return null;

		stat.local = getAddress(addrSize, token[1]);
		stat.state = State.Listen;
		int uid = Integer.parseInt(token[7]);
		if (uidToPid.containsKey(uid))
			stat.pid = uidToPid.get(uid);

		return stat;
	}
}
