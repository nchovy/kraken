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
import java.util.ArrayList;
import java.util.List;

public class NicStat {
	private NicStat() {
	}

	private String name;
	private long rxBytes;
	private long rxPackets;
	private long rxErrors;
	private long rxDrops;
	private long rxFifo;
	private long rxFrames;
	private long rxCompressed;
	private long rxMulticast;
	private long txBytes;
	private long txPackets;
	private long txErrors;
	private long txDrops;
	private long txFifo;
	private long txColls;
	private long txCarrier;
	private long txCompressed;

	public String getName() {
		return name;
	}

	public long getRxBytes() {
		return rxBytes;
	}

	public long getRxPackets() {
		return rxPackets;
	}

	public long getRxErrors() {
		return rxErrors;
	}

	public long getRxDrops() {
		return rxDrops;
	}

	public long getRxFifo() {
		return rxFifo;
	}

	public long getRxFrames() {
		return rxFrames;
	}

	public long getRxCompressed() {
		return rxCompressed;
	}

	public long getRxMulticast() {
		return rxMulticast;
	}

	public long getTxBytes() {
		return txBytes;
	}

	public long getTxPackets() {
		return txPackets;
	}

	public long getTxErrors() {
		return txErrors;
	}

	public long getTxDrops() {
		return txDrops;
	}

	public long getTxFifo() {
		return txFifo;
	}

	public long getTxColls() {
		return txColls;
	}

	public long getTxCarrier() {
		return txCarrier;
	}

	public long getTxCompressed() {
		return txCompressed;
	}

	public static List<NicStat> getNicStats() throws IOException {
		List<NicStat> stats = new ArrayList<NicStat>();
		BufferedReader br = null;
		FileInputStream is = null;

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/proc/net/dev"))));
			br.readLine(); // ignore column name
			br.readLine();
			while (true) {
				NicStat stat = new NicStat();
				String line = br.readLine();
				if (line == null)
					break;

				parse(stat, line);
				stats.add(stat);
			}

			return stats;
		} finally {
			if (is != null)
				is.close();
			if (br != null)
				br.close();
		}
	}

	private static void parse(NicStat stat, String line) {
		String[] splitted = line.split(":");
		if (splitted.length < 2)
			return;

		stat.name = splitted[0].trim();
		String[] token = splitted[1].replaceAll(" +", " ").trim().split(" ");
		if (token.length < 16)
			return;

		stat.rxBytes = Long.parseLong(token[0]);
		stat.rxPackets = Long.parseLong(token[1]);
		stat.rxErrors = Long.parseLong(token[2]);
		stat.rxDrops = Long.parseLong(token[3]);
		stat.rxFifo = Long.parseLong(token[4]);
		stat.rxFrames = Long.parseLong(token[5]);
		stat.rxCompressed = Long.parseLong(token[6]);
		stat.rxMulticast = Long.parseLong(token[7]);
		stat.txBytes = Long.parseLong(token[8]);
		stat.txPackets = Long.parseLong(token[9]);
		stat.txErrors = Long.parseLong(token[10]);
		stat.txDrops = Long.parseLong(token[11]);
		stat.txFifo = Long.parseLong(token[12]);
		stat.txColls = Long.parseLong(token[13]);
		stat.txCarrier = Long.parseLong(token[14]);
		stat.txCompressed = Long.parseLong(token[15]);
	}

	@Override
	public String toString() {
		return "name=" + name + ", rxBytes=" + rxBytes + ", rxCompressed=" + rxCompressed + ", rxDrops=" + rxDrops
				+ ", rxErrors=" + rxErrors + ", rxFifo=" + rxFifo + ", rxFrames=" + rxFrames + ", rxMulticast=" + rxMulticast
				+ ", rxPackets=" + rxPackets + ", txBytes=" + txBytes + ", txCarrier=" + txCarrier + ", txColls=" + txColls
				+ ", txCompressed=" + txCompressed + ", txDrops=" + txDrops + ", txErrors=" + txErrors + ", txFifo=" + txFifo
				+ ", txPackets=" + txPackets;
	}

}
