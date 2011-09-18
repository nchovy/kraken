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

import java.net.InetAddress;

public class RoutingEntry {
	private InetAddress destination;
	private InetAddress gateway;
	private InetAddress genmask;
	private Flag flags;
	private Integer metric;
	private Integer ref;
	private Integer use;
	private String iface;
	private Integer mss;
	private Integer window;
	private Integer irtt;

	public RoutingEntry(InetAddress destination, InetAddress gateway, InetAddress genmask, Flag flags, Integer metric,
			Integer ref, Integer use, String iface, Integer mss, Integer window, Integer irtt) {
		this.destination = destination;
		this.gateway = gateway;
		this.genmask = genmask;
		this.flags = flags;
		this.metric = metric;
		this.ref = ref;
		this.use = use;
		this.iface = iface;
		this.mss = mss;
		this.window = window;
		this.irtt = irtt;
	}

	public InetAddress getDestination() {
		return destination;
	}

	public InetAddress getGateway() {
		return gateway;
	}

	public InetAddress getGenmask() {
		return genmask;
	}

	public Flag getFlags() {
		return flags;
	}

	public Integer getMetric() {
		return metric;
	}

	public Integer getRef() {
		return ref;
	}

	public Integer getUse() {
		return use;
	}

	public String getIface() {
		return iface;
	}

	public Integer getMss() {
		return mss;
	}

	public Integer getWindow() {
		return window;
	}

	public Integer getIrtt() {
		return irtt;
	}

	public static class Flag {
		private boolean up;
		private boolean host;
		private boolean gateway;
		private boolean reinstate;
		private boolean dynamically;
		private boolean modified;
		private boolean addrconf;
		private boolean cache;
		private boolean reject;

		public Flag() {
		}

		public Flag(String flag) {
			if (flag.contains("U"))
				this.up = true;
			if (flag.contains("H"))
				this.host = true;
			if (flag.contains("G"))
				this.gateway = true;
			if (flag.contains("R"))
				this.reinstate = true;
			if (flag.contains("D"))
				this.dynamically = true;
			if (flag.contains("M"))
				this.modified = true;
			if (flag.contains("A"))
				this.addrconf = true;
			if (flag.contains("C"))
				this.cache = true;
			if (flag.contains("!"))
				this.reject = true;
		}

		public boolean isUp() {
			return up;
		}

		public boolean isHost() {
			return host;
		}

		public boolean isGateway() {
			return gateway;
		}

		public boolean isReinstate() {
			return reinstate;
		}

		public boolean isDynamically() {
			return dynamically;
		}

		public boolean isModified() {
			return modified;
		}

		public boolean isAddrconf() {
			return addrconf;
		}

		public boolean isCache() {
			return cache;
		}

		public boolean isReject() {
			return reject;
		}

		@Override
		public String toString() {
			String str = "";
			if (up)
				str += "U";
			if (host)
				str += "H";
			if (gateway)
				str += "G";
			if (reinstate)
				str += "R";
			if (dynamically)
				str += "D";
			if (modified)
				str += "M";
			if (addrconf)
				str += "A";
			if (cache)
				str += "C";
			if (reject)
				str += "!";
			return str;
		}
	}

	@Override
	public String toString() {
		return String.format("%-15s %-15s %-15s %-9s %-5d %-5d %5d %s %-4d %-6d %-4d",
				(destination != null) ? destination.getHostAddress() : "default",
				(gateway != null) ? gateway.getHostAddress() : "*", (genmask != null) ? genmask.getHostAddress() : "*",
				flags, metric, ref, use, iface, mss, window, irtt);
	}
}
