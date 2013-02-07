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
package org.krakenapps.pcap.decoder.rpce.packet;

import org.krakenapps.pcap.decoder.rpce.RpcUdpHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class UdpFackPDU implements UdpPDUInterface{

	private byte vers; // it must 0x00
	private byte pad1;
	private short windowSize;
	private int maxTsdu;
	private int maxFragSize;
	private short serialNum;
	private short selackLen;
	private int selack;
	@Override
	public void parse(Buffer b, RpcUdpHeader h) {
		vers = b.get();
		pad1 = b.get();
		windowSize = ByteOrderConverter.swap(b.getShort());
		maxTsdu = ByteOrderConverter.swap(b.getInt());
		maxFragSize = ByteOrderConverter.swap(b.getInt());
		serialNum = ByteOrderConverter.swap(b.getShort());
		selackLen = ByteOrderConverter.swap(b.getShort());
		selack = ByteOrderConverter.swap(b.getInt());
	}

	public byte getVers() {
		return vers;
	}

	public void setVers(byte vers) {
		this.vers = vers;
	}

	public byte getPad1() {
		return pad1;
	}

	public void setPad1(byte pad1) {
		this.pad1 = pad1;
	}

	public short getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(short windowSize) {
		this.windowSize = windowSize;
	}

	public int getMaxTsdu() {
		return maxTsdu;
	}

	public void setMaxTsdu(int maxTsdu) {
		this.maxTsdu = maxTsdu;
	}

	public int getMaxFragSize() {
		return maxFragSize;
	}

	public void setMaxFragSize(int maxFragSize) {
		this.maxFragSize = maxFragSize;
	}

	public short getSerialNum() {
		return serialNum;
	}

	public void setSerialNum(short serialNum) {
		this.serialNum = serialNum;
	}

	public short getSelackLen() {
		return selackLen;
	}

	public void setSelackLen(short selackLen) {
		this.selackLen = selackLen;
	}

	public int getSelack() {
		return selack;
	}

	public void setSelack(int selack) {
		this.selack = selack;
	}
}
