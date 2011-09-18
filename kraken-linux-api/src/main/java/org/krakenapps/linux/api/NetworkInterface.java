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
package org.krakenapps.linux.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

// ifconfig
public class NetworkInterface {
	private String name;
	private String linkEncap;
	private String hwaddr;
	private InetAddress inet;
	private InetAddress ptp;
	private InetAddress mask;
	private Inet6Address inet6;
	private int cidr;
	private String scope;
	private String options;
	private int mtu;
	private int metric;
	private RxPacket rxPacket;
	private TxPacket txPacket;
	private long rxBytes;
	private long txBytes;
	private int interrupt;
	private String baseAddress;
	private String memory;

	public String getName() {
		return name;
	}

	public String getLinkEncap() {
		return linkEncap;
	}

	public String getHwaddr() {
		return hwaddr;
	}

	public InetAddress getInet() {
		return inet;
	}

	public InetAddress getPtp() {
		return ptp;
	}

	public InetAddress getMask() {
		return mask;
	}

	public Inet6Address getInet6() {
		return inet6;
	}

	public int getCidr() {
		return cidr;
	}

	public String getScope() {
		return scope;
	}

	public String getOptions() {
		return options;
	}

	public int getMtu() {
		return mtu;
	}

	public int getMetric() {
		return metric;
	}

	public RxPacket getRxPacket() {
		return rxPacket;
	}

	public TxPacket getTxPacket() {
		return txPacket;
	}

	public long getRxBytes() {
		return rxBytes;
	}

	public long getTxBytes() {
		return txBytes;
	}

	public int getInterrupt() {
		return interrupt;
	}

	public String getBaseAddress() {
		return baseAddress;
	}

	public String getMemory() {
		return memory;
	}

	public static List<NetworkInterface> getNetworkInterfaces() throws IOException {
		List<NetworkInterface> ifaces = new ArrayList<NetworkInterface>();
		java.lang.Process p = null;
		BufferedReader br = null;

		try {
			p = Runtime.getRuntime().exec("ifconfig -a");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));

			while (true) {
				NetworkInterface iface = parse(br);
				if (iface == null)
					break;
				ifaces.add(iface);
			}
		} finally {
			if (p != null)
				p.destroy();
			if (br != null)
				br.close();
		}

		return ifaces;
	}

	private static NetworkInterface parse(BufferedReader br) throws IOException {
		NetworkInterface iface = null;
		String regex = "\t| {2,}";
		String line = null;

		while ((line = br.readLine()) != null && !line.trim().equals("")) {
			if (iface == null)
				iface = new NetworkInterface();

			if (iface.name == null && !line.replace("\t", " ").startsWith(" "))
				iface.name = line.trim().replace("\t", " ").substring(0, line.trim().replace("\t", " ").indexOf(" "));

			String[] tokens = line.trim().split(regex);
			for (String token : tokens) {
				if (token.contains(":")) {
					String key = token.split(":")[0];
					String value = token.split(":", 2)[1];

					if (key.equals("Link encap"))
						iface.linkEncap = value;
					else if (key.equals("inet addr"))
						iface.inet = InetAddress.getByName(value);
					else if (key.equals("P-t-P"))
						iface.ptp = InetAddress.getByName(value);
					else if (key.equals("Mask"))
						iface.mask = InetAddress.getByName(value);
					else if (key.equals("inet6 addr")) {
						value = value.trim();
						iface.inet6 = (Inet6Address) Inet6Address.getByName(value.substring(0, value.indexOf("/")));
						iface.cidr = Integer.parseInt(value.substring(value.indexOf("/") + 1, value.lastIndexOf(" ")));
						iface.scope = value.substring(value.lastIndexOf(":") + 1);
					} else if (key.equals("MTU"))
						iface.mtu = Integer.parseInt(value);
					else if (key.equals("Metric"))
						iface.metric = Integer.parseInt(value);
					else if (key.equals("RX packets"))
						iface.rxPacket = new RxPacket(token.trim());
					else if (key.equals("TX packets"))
						iface.txPacket = new TxPacket(token.trim() + " " + br.readLine().trim());
					else if (key.equals("RX bytes"))
						iface.rxBytes = Long.parseLong(value.split(" ")[0]);
					else if (key.equals("TX bytes"))
						iface.txBytes = Long.parseLong(value.split(" ")[0]);
					else if (key.equals("Interrupt")) {
						if (value.contains(" ")) {
							iface.interrupt = Integer.parseInt(value.substring(0, value.indexOf(" ")));
							iface.baseAddress = value.substring(value.lastIndexOf(":") + 1);
						} else
							iface.interrupt = Integer.parseInt(value);
					} else if (key.equals("Memory"))
						iface.memory = value;
				} else {
					if (token.matches("[A-Z ]+"))
						iface.options = token;
					else if (token.contains(" ")) {
						if (token.startsWith("HWaddr "))
							iface.hwaddr = token.substring("HWaddr ".length());
					}
				}
			}
		}

		return iface;
	}

	public static class RxPacket {
		private int packets;
		private int errors;
		private int dropped;
		private int overruns;
		private int frame;

		public RxPacket(String str) {
			String[] tokens = str.split(" ");

			if (!tokens[0].equals("RX"))
				throw new IllegalArgumentException("isn't ifconfig rx packet format");

			for (String token : tokens) {
				if (!token.contains(":"))
					continue;

				String key = token.split(":")[0];
				String value = token.split(":")[1];

				if (key.equals("packets"))
					this.packets = Integer.parseInt(value);
				else if (key.equals("errors"))
					this.errors = Integer.parseInt(value);
				else if (key.equals("dropped"))
					this.dropped = Integer.parseInt(value);
				else if (key.equals("overruns"))
					this.overruns = Integer.parseInt(value);
				else if (key.equals("frame"))
					this.frame = Integer.parseInt(value);
			}
		}

		public int getPackets() {
			return packets;
		}

		public int getErrors() {
			return errors;
		}

		public int getDropped() {
			return dropped;
		}

		public int getOverruns() {
			return overruns;
		}

		public int getFrame() {
			return frame;
		}

		@Override
		public String toString() {
			return "RxPacket [packets=" + packets + ", errors=" + errors + ", dropped=" + dropped + ", overruns=" + overruns
					+ ", frame=" + frame + "]";
		}
	}

	public static class TxPacket {
		private int packets;
		private int errors;
		private int dropped;
		private int overruns;
		private int carrier;
		private int collisions;
		private int queuelen;

		public TxPacket(String str) {
			String[] tokens = str.split(" ");

			if (!tokens[0].equals("TX"))
				throw new IllegalArgumentException("isn't ifconfig tx packet format");

			for (String token : tokens) {
				if (!token.contains(":"))
					continue;

				String key = token.split(":")[0];
				String value = token.split(":")[1];

				if (key.equals("packets"))
					this.packets = Integer.parseInt(value);
				else if (key.equals("errors"))
					this.errors = Integer.parseInt(value);
				else if (key.equals("dropped"))
					this.dropped = Integer.parseInt(value);
				else if (key.equals("overruns"))
					this.overruns = Integer.parseInt(value);
				else if (key.equals("carrier"))
					this.carrier = Integer.parseInt(value);
				else if (key.equals("collisions"))
					this.collisions = Integer.parseInt(value);
				else if (key.equals("txqueuelen"))
					this.queuelen = Integer.parseInt(value);
			}
		}

		public int getPackets() {
			return packets;
		}

		public int getErrors() {
			return errors;
		}

		public int getDropped() {
			return dropped;
		}

		public int getOverruns() {
			return overruns;
		}

		public int getCarrier() {
			return carrier;
		}

		public int getCollisions() {
			return collisions;
		}

		public int getQueuelen() {
			return queuelen;
		}

		@Override
		public String toString() {
			return "TxPacket [packets=" + packets + ", errors=" + errors + ", dropped=" + dropped + ", overruns=" + overruns
					+ ", carrier=" + carrier + ", collisions=" + collisions + ", queuelen=" + queuelen + "]";
		}
	}

	@Override
	public String toString() {
		return "NetworkInterface [name=" + name + ", linkEncap=" + linkEncap + ", hwaddr=" + hwaddr + ", inet=" + inet + ", ptp="
				+ ptp + ", mask=" + mask + ", inet6=" + inet6 + ", cidr=" + cidr + ", scope=" + scope + ", options=" + options
				+ ", mtu=" + mtu + ", metric=" + metric + ", rxPacket=" + rxPacket + ", txPacket=" + txPacket + ", rxBytes="
				+ rxBytes + ", txBytes=" + txBytes + ", interrupt=" + interrupt + ", baseAddress=" + baseAddress + ", memory="
				+ memory + "]";
	}
}
