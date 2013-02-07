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

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.util.Buffer;

public class NbstatResourceRecord extends ResourceRecord {

	// follow add NAME_Flags
	public final static short G = (short) 0x8000;
	public final static short ONT = (short) 0x6000;
	public final static short DRG = (short) 0x1000;
	public final static short CNF = (short) 0x0800;
	public final static short ACT = (short) 0x0400;
	public final static short PRM = (short) 0x0200;

	private int ttl;
	private short rdLength;
	private NodeName[] nameArray;
	private Statistics statistics = new Statistics();

	
	public NbstatResourceRecord(String name) {
		super(name);
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public short getRdLength() {
		return rdLength;
	}

	public void setRdLength(short rdLength) {
		this.rdLength = rdLength;
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public void setStatistics(int count, Buffer b) {
		statistics.allocUnitID(6);
		byte[] buff = new byte[6];
		statistics.setUnitID(buff);
		statistics.setJumpers(b.get());
		statistics.setTestResult(b.get());
		statistics.setVersionNum(b.getShort());
		statistics.setPeriodOfStatistics(b.getShort());
		statistics.setNumberOfCRCs(b.getShort());
		statistics.setNumberAlignmentErrors(b.getShort());
		statistics.setNumberOfCollisions(b.getShort());
		statistics.setNumberSendAborts(b.getShort());
		statistics.setNumberGoodSends(b.getInt());
		statistics.setNumberGoodReceives(b.getInt());
		statistics.setNumberRetransmits(b.getShort());
		statistics.setNumberNoResourceConditions(b.getShort());
		statistics.setNumberFreeCommandBlocks(b.getShort());
		statistics.setTotalNumberCommandBlocks(b.getShort());
		statistics.setMaxTotalNumberCommandBlocks(b.getShort());
		statistics.setNumberPendingSessions(b.getShort());
		statistics.setMaxNumberPendingSessions(b.getShort());
		statistics.setMaxTotalSessionPossible(b.getShort());
		statistics.setSessionDataPacketSize(b.getShort());
	}

	public short isGDatafield(NodeName data) {
		return (short) ((data.getFlags() & G) >> 15);
		// 0 = RR_NAME is unique name
		// 1 = RR_NAME is group name
	}

	public short isONTDatafield(NodeName data) {
		return (short) ((data.getFlags() & ONT) >> 13);
		// 00 = B node
		// 01 = P node
		// 10 = M node
		// 11 = Reservedfor guture use
	}

	public short isDRGDatafield(NodeName data) {
		return (short) ((data.getFlags() & DRG) >> 12);
	}

	public short isCNFDatafield(NodeName data) {
		return (short) ((data.getFlags() & CNF) >> 11);
	}

	public short isACTDatafield(NodeName data) {
		return (short) ((data.getFlags() & ACT) >> 10);
	}

	public short isPRMDatafield(NodeName data) {
		return (short) ((data.getFlags() & PRM) >> 9);
	}

	public void setRData(Buffer b) {

		int nameCount = 0;
		int offset = 0;
		byte[] buffer;
		nameCount = b.get();
		nameArray = new NodeName[nameCount];
		for (int i = 0; i < nameCount; i++) {
			// parse node name
			offset = b.bytesBefore(new byte[] { 0x00 });
			buffer = new byte[offset - 1];
			b.get();
			b.gets(buffer);
			b.get();
			String name = NetBiosNameCodec.decodeResourceName(buffer);

			// parse name flags
			int flags = b.getShort() & 0xffff;

			nameArray[i] = new NodeName(name, flags);
		}
		setStatistics(nameCount, b);
	}

	@Override
	public void parse(Buffer b, int type) {
		this.setType(Type.parse(type));
		this.setCls(b.getShort());
		this.setTtl(b.getInt());
		this.setRdLength(b.getShort());
		this.setRData(b);
	}
	@Override
	public String toString(){
		return String.format("ResoruceRecord(NbResourceRecord)\n"+
				"type = %s , cls(class) = 0x%s , ttl = 0x%s\n"+
				"Rdlength = 0x%s\n"
				, this.type , Integer.toHexString(this.cls) , Integer.toHexString(this.ttl),
				Integer.toHexString(this.rdLength)) + statistics.toString();
	}
}
