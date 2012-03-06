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
package org.krakenapps.pcap.decoder.dhcp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.krakenapps.pcap.decoder.dhcp.options.DhcpOption;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;

/**
 * See http://tools.ietf.org/html/rfc2131#page-9
 * 
 * @author xeraph
 * 
 */
public class DhcpMessage {
	public enum Type {
		Unknown(0), Discover(1), Offer(2), Request(3), Decline(4), Ack(5), Nak(6), Release(7), Inform(8);

		private int value;

		Type(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}
		
		public static Type from(int value) {
			for (Type t : Type.values())
				if (t.value() == value)
					return t;
			
			return Unknown;
		}
	}

	private byte messageType;
	private byte hardwareType;
	private byte hardwareAddressLength;
	private byte hops;
	private int transactionId;
	private short secs;
	private short flags;
	private InetAddress clientAddress;
	private InetAddress yourAddress;
	private InetAddress nextServerAddress;
	private InetAddress gatewayAddress;
	private MacAddress clientMac;
	private String serverName;
	private String bootFileName;
	private List<DhcpOption> options = new ArrayList<DhcpOption>();

	public byte getMessageType() {
		return messageType;
	}

	public void setMessageType(byte messageType) {
		this.messageType = messageType;
	}

	public byte getHardwareType() {
		return hardwareType;
	}

	public void setHardwareType(byte hardwareType) {
		this.hardwareType = hardwareType;
	}

	public byte getHardwareAddressLength() {
		return hardwareAddressLength;
	}

	public void setHardwareAddressLength(byte hardwareAddressLength) {
		this.hardwareAddressLength = hardwareAddressLength;
	}

	public byte getHops() {
		return hops;
	}

	public void setHops(byte hops) {
		this.hops = hops;
	}

	public int getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}

	public short getSecs() {
		return secs;
	}

	public void setSecs(short secs) {
		this.secs = secs;
	}

	public short getFlags() {
		return flags;
	}

	public void setFlags(short flags) {
		this.flags = flags;
	}

	public InetAddress getClientAddress() {
		return clientAddress;
	}

	public void setClientAddress(InetAddress clientAddress) {
		this.clientAddress = clientAddress;
	}

	public InetAddress getYourAddress() {
		return yourAddress;
	}

	public void setYourAddress(InetAddress yourAddress) {
		this.yourAddress = yourAddress;
	}

	public InetAddress getNextServerAddress() {
		return nextServerAddress;
	}

	public void setNextServerAddress(InetAddress nextServerAddress) {
		this.nextServerAddress = nextServerAddress;
	}

	public InetAddress getGatewayAddress() {
		return gatewayAddress;
	}

	public void setGatewayAddress(InetAddress gatewayAddress) {
		this.gatewayAddress = gatewayAddress;
	}

	public MacAddress getClientMac() {
		return clientMac;
	}

	public void setClientMac(MacAddress clientMac) {
		this.clientMac = clientMac;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getBootFileName() {
		return bootFileName;
	}

	public void setBootFileName(String bootFileName) {
		this.bootFileName = bootFileName;
	}

	public List<DhcpOption> getOptions() {
		return options;
	}

	public void setOptions(List<DhcpOption> options) {
		this.options = options;
	}

	public DhcpOption getOption(int type) {
		for (DhcpOption option : getOptions())
			if (option.getType() == type)
				return option;

		return null;

	}
}
