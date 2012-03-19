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
 */package org.krakenapps.linux.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

// CentOS (/etc/sysconfig/network-scripts/devname)
public class EthernetInterface {
	private static final String cfgPath = "/etc/sysconfig/network-scripts/";

	public enum Bootproto {
		None, BOOTP, DHCP
	};

	private Bootproto bootProto;
	private InetAddress broadcast;
	private String device;
	private InetAddress dhcpHostname;
	private InetAddress dns1;
	private InetAddress dns2;
	private String ethtoolOpts;
	private InetAddress gateway;
	private String hwAddr;
	private InetAddress ipAddr;
	private String macAddr;
	private String master;
	private InetAddress netmask;
	private InetAddress network;
	private Boolean onBoot;
	private Boolean peerDns;
	private Boolean slave;
	private InetAddress srcAdrr;
	private Boolean userCtl;
	private List<String> description;
	private List<AddressBinding> addressBindings;

	public EthernetInterface(String device) throws IOException {
		BufferedReader br = null;
		String line = null;

		if (device.contains(":"))
			device = device.substring(0, device.indexOf(":"));

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(cfgPath + "ifcfg-" + device))));
			this.description = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				if (line.contains("=")) {
					String key = line.split("=", 2)[0].trim();
					String value = line.split("=", 2)[1].trim();

					if (key.equals("BOOTPROTO")) {
						if (value.equalsIgnoreCase("none"))
							this.bootProto = Bootproto.None;
						else if (value.equalsIgnoreCase("bootp"))
							this.bootProto = Bootproto.BOOTP;
						else if (value.equalsIgnoreCase("dhcp")) {
							this.bootProto = Bootproto.DHCP;
							this.peerDns = true;
						}
					} else if (key.equals("BROADCAST"))
						this.broadcast = InetAddress.getByName(value);
					else if (key.equals("DEVICE"))
						this.device = value;
					else if (key.equals("DHCP_HOSTNAME"))
						this.dhcpHostname = InetAddress.getByName(value);
					else if (key.equals("DNS1"))
						this.dns1 = InetAddress.getByName(value);
					else if (key.equals("DNS2"))
						this.dns2 = InetAddress.getByName(value);
					else if (key.equals("ETHTOOL_OPTS"))
						this.ethtoolOpts = value;
					else if (key.equals("GATEWAY"))
						this.gateway = InetAddress.getByName(value);
					else if (key.equals("HWADDR"))
						this.hwAddr = value;
					else if (key.equals("IPADDR"))
						this.ipAddr = InetAddress.getByName(value);
					else if (key.equals("MACADDR"))
						this.macAddr = value;
					else if (key.equals("MASTER"))
						this.master = value;
					else if (key.equals("NETMASK"))
						this.netmask = InetAddress.getByName(value);
					else if (key.equals("NETWORK"))
						this.network = InetAddress.getByName(value);
					else if (key.equals("ONBOOT"))
						this.onBoot = value.equals("yes") ? true : false;
					else if (key.equals("PEERDNS"))
						this.peerDns = value.equals("yes") ? true : false;
					else if (key.equals("SLAVE"))
						this.slave = value.equals("yes") ? true : false;
					else if (key.equals("SRCADDR"))
						this.srcAdrr = InetAddress.getByName(value);
					else if (key.equals("USERCTL"))
						this.userCtl = value.equals("yes") ? true : false;
				} else {
					description.add(line);
				}
			}
		} finally {
			if (br != null)
				br.close();
		}

		this.addressBindings = new ArrayList<AddressBinding>();
		File[] sublist = new File(cfgPath).listFiles();
		for (File sub : sublist) {
			if (!sub.isFile())
				continue;
			String filename = sub.getName();
			if (!filename.startsWith("ifcfg-" + device) || !filename.contains(":"))
				continue;

			AddressBinding subAddr = new AddressBinding();
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(sub)));
				while ((line = br.readLine()) != null) {
					if (line.contains("=")) {
						String key = line.split("=", 2)[0].trim();
						String value = line.split("=", 2)[1].trim();

						if (key.equals("DEVICE"))
							subAddr.device = value;
						else if (key.equals("IPADDR"))
							subAddr.ipAddr = InetAddress.getByName(value);
						else if (key.equals("BROADCAST"))
							subAddr.broadcast = InetAddress.getByName(value);
						else if (key.equals("NETMASK"))
							subAddr.netmask = InetAddress.getByName(value);
						else
							subAddr.description.add(line);
					} else
						subAddr.description.add(line);
				}
			} finally {
				if (br != null)
					br.close();
			}
			
			addressBindings.add(subAddr);
		}
	}

	public static List<EthernetInterface> getEthernetInterfaces() throws IOException {
		List<EthernetInterface> interfaces = new ArrayList<EthernetInterface>();
		String[] filenames = new File(cfgPath).list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (name.startsWith("ifcfg-") && !name.endsWith(".bak"));
			}
		});

		for (String filename : filenames) {
			try {
				EthernetInterface eth = new EthernetInterface(filename.substring("ifcfg-".length()));
				interfaces.add(eth);
			} catch (FileNotFoundException e) {
			}
		}

		return interfaces;
	}

	public void save() {
		PrintStream pStream = null;

		try {
			pStream = new PrintStream(new File(cfgPath + "ifcfg-" + device));
			for (String desc : description)
				pStream.println(desc);
			if (bootProto != null)
				pStream.println("BOOTPROTO=" + bootProto.toString().toLowerCase());
			if (broadcast != null)
				pStream.println("BROADCAST=" + broadcast.getHostAddress());
			pStream.println("DEVICE=" + device);
			if (dhcpHostname != null)
				pStream.println("DHCP_HOSTNAME=" + dhcpHostname.getHostAddress());
			if (dns1 != null) {
				pStream.println("DNS1=" + dns1.getHostAddress());
				if (dns2 != null)
					pStream.println("DNS2=" + dns2.getHostAddress());
			}
			if (ethtoolOpts != null)
				pStream.println("ETHTOOL_OPTS=" + ethtoolOpts);
			if (gateway != null)
				pStream.println("GATEWAY=" + gateway.getHostAddress());
			if (hwAddr != null)
				pStream.println("HWADDR=" + hwAddr);
			if (ipAddr != null)
				pStream.println("IPADDR=" + ipAddr.getHostAddress());
			if (macAddr != null)
				pStream.println("MACADDR=" + macAddr);
			if (master != null)
				pStream.println("MASTER=" + master);
			if (netmask != null)
				pStream.println("NETMASK=" + netmask.getHostAddress());
			if (network != null)
				pStream.println("NETWORK=" + network.getHostAddress());
			if (onBoot != null)
				pStream.println("ONBOOT=" + (onBoot ? "yes" : "no"));
			if (peerDns != null)
				pStream.println("PEERDNS=" + (peerDns ? "yes" : "no"));
			if (slave != null)
				pStream.println("SLAVE=" + (slave ? "yes" : "no"));
			if (srcAdrr != null)
				pStream.println("SRCADDR=" + srcAdrr.getHostAddress());
			if (userCtl != null)
				pStream.println("USERCTL=" + (userCtl ? "yes" : "no"));
			pStream.close();

			for (AddressBinding sub : addressBindings) {
				pStream = new PrintStream(new File(cfgPath + "ifcfg-" + sub.device));
				if (sub.ipAddr != null)
					pStream.println("IPADDR=" + sub.ipAddr.getHostAddress());
				if (sub.broadcast != null)
					pStream.println("BROADCAST=" + sub.broadcast.getHostAddress());
				if (sub.netmask != null)
					pStream.println("NETMASK=" + sub.netmask.getHostAddress());
				for (String str : sub.description)
					pStream.println(str);
				pStream.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public String up() {
		return Util.run("ifup " + device);
	}

	public String down() {
		return Util.run("ifdown " + device);
	}

	public static String restart() {
		return Util.run("service network restart");
	}

	public Bootproto getBootProto() {
		return bootProto;
	}

	public void setBootProto(Bootproto bootProto) {
		this.bootProto = bootProto;
	}

	public InetAddress getBroadcast() {
		return broadcast;
	}

	public void setBroadcast(InetAddress broadcast) {
		this.broadcast = broadcast;
	}

	public String getDevice() {
		return device;
	}

	public InetAddress getDhcpHostname() {
		return dhcpHostname;
	}

	public void setDhcpHostname(InetAddress dhcpHostname) {
		this.dhcpHostname = dhcpHostname;
	}

	public InetAddress getDns1() {
		return dns1;
	}

	public void setDns1(InetAddress dns1) {
		this.dns1 = dns1;
	}

	public InetAddress getDns2() {
		return dns2;
	}

	public void setDns2(InetAddress dns2) {
		this.dns2 = dns2;
	}

	public String getEthtoolOpts() {
		return ethtoolOpts;
	}

	public void setEthtoolOpts(String ethtoolOpts) {
		this.ethtoolOpts = ethtoolOpts;
	}

	public InetAddress getGateway() {
		return gateway;
	}

	public void setGateway(InetAddress gateway) {
		this.gateway = gateway;
	}

	public String getHwAddr() {
		return hwAddr;
	}

	public void setHwAddr(String hwAddr) {
		this.hwAddr = hwAddr;
	}

	public InetAddress getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(InetAddress ipAddr) {
		this.ipAddr = ipAddr;
	}

	public String getMacAddr() {
		return macAddr;
	}

	public void setMacAddr(String macAddr) {
		this.macAddr = macAddr;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}

	public InetAddress getNetmask() {
		return netmask;
	}

	public void setNetmask(InetAddress netmask) {
		this.netmask = netmask;
	}

	public InetAddress getNetwork() {
		return network;
	}

	public void setNetwork(InetAddress network) {
		this.network = network;
	}

	public Boolean getOnBoot() {
		return onBoot;
	}

	public void setOnBoot(Boolean onBoot) {
		this.onBoot = onBoot;
	}

	public Boolean getPeerDns() {
		return peerDns;
	}

	public void setPeerDns(Boolean peerDns) {
		this.peerDns = peerDns;
	}

	public Boolean getSlave() {
		return slave;
	}

	public void setSlave(Boolean slave) {
		this.slave = slave;
	}

	public InetAddress getSrcAdrr() {
		return srcAdrr;
	}

	public void setSrcAdrr(InetAddress srcAdrr) {
		this.srcAdrr = srcAdrr;
	}

	public Boolean getUserCtl() {
		return userCtl;
	}

	public void setUserCtl(Boolean userCtl) {
		this.userCtl = userCtl;
	}

	public List<String> getDescription() {
		return description;
	}

	public void setDescription(List<String> description) {
		this.description = description;
	}

	public List<AddressBinding> getAddressBindings() {
		return addressBindings;
	}

	public void addAddressBinding(InetAddress ipAddr) {
		int num;
		for (num = 0;; num++) {
			if (!new File(cfgPath + "ifcfg-" + device + ":" + num).exists())
				break;
		}

		AddressBinding sub = new AddressBinding();
		sub.parent = this;
		sub.device = device + ":" + num;
		sub.ipAddr = ipAddr;
		addressBindings.add(sub);
	}

	public class AddressBinding {
		private EthernetInterface parent;
		private String device;
		private InetAddress ipAddr;
		private InetAddress broadcast;
		private InetAddress netmask;
		private List<String> description;

		private AddressBinding() {
			this.description = new ArrayList<String>();
		}

		public String getDevice() {
			return device;
		}

		public InetAddress getIpAddr() {
			return ipAddr;
		}

		public void setIpAddr(InetAddress ipAddr) {
			this.ipAddr = ipAddr;
		}

		public InetAddress getBroadcast() {
			return broadcast;
		}

		public void setBroadcast(InetAddress broadcast) {
			this.broadcast = broadcast;
		}

		public InetAddress getNetmask() {
			return netmask;
		}

		public void setNetmask(InetAddress netmask) {
			this.netmask = netmask;
		}

		public void down() {
			parent.addressBindings.remove(this);
			new File(cfgPath + "ifcfg-" + device).delete();
		}
	}

}
