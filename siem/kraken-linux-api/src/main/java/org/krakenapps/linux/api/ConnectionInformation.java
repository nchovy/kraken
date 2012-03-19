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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public abstract class ConnectionInformation {
	public enum State {
		Unknown, Established, SynSent, SynRecv, FinWait1, FinWait2, TimeWait, Close, CloseWait, LastACK, Listen, Closing
	}

	public abstract InetSocketAddress getLocal();

	public abstract InetSocketAddress getRemote();

	public abstract State getState();

	public abstract int getPid();

	protected static Map<Integer, Integer> getUidToPidMap(String type) {
		Map<String, Integer> uids = new HashMap<String, Integer>();
		BufferedReader br = null;
		String line = null;

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/etc/passwd"))));
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(":");
				uids.put(tokens[0], Integer.parseInt(tokens[2]));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Map<Integer, Integer> uidToPid = new HashMap<Integer, Integer>();
		try {
			java.lang.Process p = Runtime.getRuntime().exec("lsof -ni " + type);
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));

			br.readLine(); // ignore column name line
			while ((line = br.readLine()) != null) {
				line = line.replaceAll(" +", " ");
				String[] tokens = line.split(" ");
				if (!uids.containsKey(tokens[2]))
					continue;

				uidToPid.put(uids.get(tokens[2]), Integer.parseInt(tokens[1]));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
		}

		return uidToPid;
	}

	protected static InetSocketAddress getAddress(int addrSize, String str) {
		InetSocketAddress ret = null;
		String[] token = str.split(":");
		byte[] addr = new byte[addrSize];

		for (int i = 0; i < addrSize; i++)
			addr[addrSize - 1 - i] = (byte) Integer.parseInt(token[0].substring(i * 2, i * 2 + 2), 16);
		try {
			ret = new InetSocketAddress(InetAddress.getByAddress(addr), Integer.parseInt(token[1], 16));
		} catch (UnknownHostException e) {
		}

		return ret;
	}
}
