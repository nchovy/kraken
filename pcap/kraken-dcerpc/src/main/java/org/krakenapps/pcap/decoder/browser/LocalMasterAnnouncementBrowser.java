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
package org.krakenapps.pcap.decoder.browser;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.rpce.RpcUdpHeader;
import org.krakenapps.pcap.decoder.rpce.packet.UdpPDUInterface;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class LocalMasterAnnouncementBrowser implements UdpPDUInterface {

	byte opcode;
	byte updateCount;
	int periodicity;
	String serverName;// 16
	byte osVersionMajor;
	byte osVersionMinor;
	int serverType;
	byte browserConfigVersionMajor;
	byte browserConfigVersionMinor;
	short signature;
	String comments; // null terminated string

	public byte getOpcode() {
		return opcode;
	}

	public void setOpcode(byte opcode) {
		this.opcode = opcode;
	}

	public byte getUpdateCount() {
		return updateCount;
	}

	public void setUpdateCount(byte updateCount) {
		this.updateCount = updateCount;
	}

	public int getPeriodicity() {
		return periodicity;
	}

	public void setPeriodicity(int periodicity) {
		this.periodicity = periodicity;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public byte getOsVersionMajor() {
		return osVersionMajor;
	}

	public void setOsVersionMajor(byte osVersionMajor) {
		this.osVersionMajor = osVersionMajor;
	}

	public byte getOsVersionMinor() {
		return osVersionMinor;
	}

	public void setOsVersionMinor(byte osVersionMinor) {
		this.osVersionMinor = osVersionMinor;
	}

	public int getServerType() {
		return serverType;
	}

	public void setServerType(int serverType) {
		this.serverType = serverType;
	}

	public byte getBrowserConfigVersionMajor() {
		return browserConfigVersionMajor;
	}

	public void setBrowserConfigVersionMajor(byte browserConfigVersionMajor) {
		this.browserConfigVersionMajor = browserConfigVersionMajor;
	}

	public byte getBrowserConfigVersionMinor() {
		return browserConfigVersionMinor;
	}

	public void setBrowserConfigVersionMinor(byte browserConfigVersionMinor) {
		this.browserConfigVersionMinor = browserConfigVersionMinor;
	}

	public short getSignature() {
		return signature;
	}

	public void setSignature(short signature) {
		this.signature = signature;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	@Override
	public void parse(Buffer b, RpcUdpHeader h) {
		opcode = b.get();
		updateCount = b.get();
		periodicity = ByteOrderConverter.swap(b.getInt());
		serverName = NetBiosNameCodec.readOemName(b, 16);
		osVersionMajor = b.get();
		osVersionMinor = b.get();
		serverType = ByteOrderConverter.swap(b.getInt());
		browserConfigVersionMajor = b.get();
		browserConfigVersionMinor = b.get();
		signature = ByteOrderConverter.swap(b.getShort());
		comments = NetBiosNameCodec.readOemName(b); // null terminated string


	}

}
