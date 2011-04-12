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
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.net.InetSocketAddress;

public class TcpConnectionInformation extends ConnectionInformation {
	private TcpConnectionInformation() {
	}

	private InetSocketAddress local;
	private InetSocketAddress remote;
	private State state;
	private int pid;

	@Override
	public InetSocketAddress getLocal() {
		return local;
	}

	@Override
	public InetSocketAddress getRemote() {
		return remote;
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public int getPid() {
		return pid;
	}

	public static List<ConnectionInformation> getTcpInformations() throws IOException {
		Map<Integer, Integer> uidToPid = getUidToPidMap("TCP");
		return getTcpInformations("/proc/net/tcp", uidToPid);
	}

	public static List<ConnectionInformation> getTcp6Informations() throws IOException {
		Map<Integer, Integer> uidToPid = getUidToPidMap("TCP");
		return getTcpInformations("/proc/net/tcp6", uidToPid);
	}

	public static List<ConnectionInformation> getAllTcpInformations() throws IOException {
		List<ConnectionInformation> stats = new ArrayList<ConnectionInformation>();
		Map<Integer, Integer> uidToPid = getUidToPidMap("TCP");

		stats.addAll(getTcpInformations("/proc/net/tcp", uidToPid));
		stats.addAll(getTcpInformations("/proc/net/tcp6", uidToPid));

		return stats;
	}

	private static List<ConnectionInformation> getTcpInformations(String filePath, Map<Integer, Integer> uidToPid)
			throws IOException {
		List<ConnectionInformation> stats = new ArrayList<ConnectionInformation>();
		BufferedReader br = null;

		br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
		br.readLine(); // ignore column name line
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;

			if (filePath.endsWith("tcp"))
				stats.add(parse(line, 4, uidToPid));
			else if (filePath.endsWith("tcp6"))
				stats.add(parse(line, 16, uidToPid));
		}

		try {
			br.close();
		} catch (IOException e) {
		}

		return stats;
	}

	private static TcpConnectionInformation parse(String str, int addrSize, Map<Integer, Integer> uidToPid) {
		TcpConnectionInformation stat = new TcpConnectionInformation();

		String[] token = str.trim().replaceAll(" +", " ").split(" ");
		if (token.length < 12)
			return null;

		stat.local = getAddress(addrSize, token[1]);
		stat.remote = getAddress(addrSize, token[2]);
		stat.state = State.values()[Integer.parseInt(token[3], 16)];
		int uid = Integer.parseInt(token[7]);
		if (uidToPid.containsKey(uid))
			stat.pid = uidToPid.get(uid);

		return stat;
	}
}
