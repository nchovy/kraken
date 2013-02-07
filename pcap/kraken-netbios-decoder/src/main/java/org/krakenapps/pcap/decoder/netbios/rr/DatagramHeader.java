/*
 * Copyright 2011 Future Systems, Inc
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
package org.krakenapps.pcap.decoder.netbios.rr;

import java.net.InetAddress;

import org.krakenapps.pcap.decoder.netbios.NetBiosDatagramType;

abstract public class DatagramHeader {
	public final static byte SNT = (byte) 0x06;
	public final static byte F = (byte) 0x02;
	public final static byte M = (byte) 0x01;
	protected NetBiosDatagramType msgType;
	protected byte flags;
	protected short dgmID;
	protected InetAddress addresses;
	protected short port;

	public NetBiosDatagramType getMsgType() {
		return msgType;
	}

	public void setMsgType(NetBiosDatagramType msgType) {
		this.msgType = msgType;
	}

	public byte getFlags() {
		return flags;
	}

	public void setFlags(byte flags) {
		this.flags = flags;
	}

	public short getDgmID() {
		return dgmID;
	}

	public void setDgmID(short dgmID) {
		this.dgmID = dgmID;
	}

	public InetAddress getAddresses() {
		return addresses;
	}

	public void setAddresses(InetAddress addresses) {
		this.addresses = addresses;
	}

	public short getPort() {
		return port;
	}

	public void setPort(short port) {
		this.port = port;
	}

	public int isM() {
		return (int) (this.flags & M);
	}

	public int isF() {
		return (int) ((this.flags & F) >> 1);
	}

	public int isSNT() {
		return (int) ((this.flags & SNT) >> 2);
	}
	@Override
	public String toString(){
		return String.format("DatagramHeader\n"+
				"msgType = %s , flags = 0x%s , dgmID = 0x%s\n"+
				"address = %s , port = 0x%s\n",
				this,msgType , Integer.toHexString(this.flags) , Integer.toHexString(this.dgmID) , 
				this.addresses , Integer.toHexString(this.port));
		
	}
}
