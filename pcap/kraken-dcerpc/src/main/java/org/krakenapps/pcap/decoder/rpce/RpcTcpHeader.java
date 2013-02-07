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
package org.krakenapps.pcap.decoder.rpce;

import org.krakenapps.pcap.decoder.rpce.rr.TcpPDUType;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class RpcTcpHeader {

	public final static byte PFC_FIRST_FRAG = 0x01;
	public final static byte PFC_LAST_FRAG = 0x02;
	public final static byte PFC_PENDING_CANCEL = 0x04;
	public final static byte PFC_RESERVED_1 = 0x08;
	public final static byte PFC_CONC_MPX = 0x10;
	public final static byte PFC_DID_NOT_EXECUTE = 0x20;
	public final static byte MAY_BE = 0x40;
	public final static byte PFC_OBJECT_UUID = (byte)0x80;
	
	// first Flags
	public final byte FLAG1_RESERVED01 = 0x01;
	public final byte FLAG1_LAST_FRAG = 0x02;
	public final byte FLAG1_FRAG = 0x04;
	public final byte FLAG1_NO_FACK = 0x08;
	public final byte FLAG1_MAY_BE = 0x10;
	public final byte FLAG1_IDEM_POTENT = 0x20;
	public final byte FLAG1_BROADCAST = 0x40;
	public final byte FLAG1_RESERVED02 = (byte)0x80;
	
	// second Flags
	public final byte FLAG2_RESERVED01 = 0x01;
	public final byte FLAG2_CANCEL_PENDING = 0x02;
	public final byte FLAG2_RESERVED04 = 0x04;
	public final byte FLAG2_RESERVED08 = 0x08;
	public final byte FLAG2_RESERVED10 = 0x10;
	public final byte FLAG2_RESERVED20 = 0x20;
	public final byte FLAG2_RESERVED40 = 0x40;
	public final byte FLAG2_RESERVED80 = (byte)0x80;
	// common field
	private byte rpcVers;
	private byte rpcVersMinor;
	private TcpPDUType ptype;
	private byte pfc_flags;
	private byte[] packedDrep; // algin 4byte
	private short fragLength;
	private short authLength;
	private int callId;
	// end of common field
	public RpcTcpHeader(){
		packedDrep = new byte[4];
	}
	public void parse(Buffer b){
		rpcVers = b.get();
		rpcVersMinor = b.get();
		ptype = TcpPDUType.parse(b.get());
		pfc_flags = b.get();
		b.gets(packedDrep);
		fragLength = ByteOrderConverter.swap(b.getShort());
		authLength = ByteOrderConverter.swap(b.getShort());
		callId = ByteOrderConverter.swap(b.getInt());
//		System.out.println("vers = " + rpcVers);
//		System.out.println("versMinor = " + rpcVersMinor);
//		System.out.println("ptype = " + ptype);
//		System.out.println("pfc_flags = " + pfc_flags);
//		System.out.println("gragLength = " + fragLength);
//		System.out.println("authLength = " + authLength);
//		System.out.println("callID = " + callId);
	}

	public TcpPDUType getPtype() {
		return ptype;
	}
	public void setPtype(TcpPDUType ptype) {
		this.ptype = ptype;
	}
	public byte getRpcVers() {
		return rpcVers;
	}

	public void setRpcVers(byte rpcVers) {
		this.rpcVers = rpcVers;
	}

	public byte getRpcVersMinor() {
		return rpcVersMinor;
	}

	public void setRpcVersMinor(byte rpcVersMinor) {
		this.rpcVersMinor = rpcVersMinor;
	}

	public byte getPfc_flags() {
		return pfc_flags;
	}

	public void setPfc_flags(byte pfc_flags) {
		this.pfc_flags = pfc_flags;
	}

	public byte[] getPackedDrep() {
		return packedDrep;
	}

	public void setPackedDrep(byte[] packedDrep) {
		this.packedDrep = packedDrep;
	}

	public short getFragLength() {
		return fragLength;
	}

	public void setFragLength(short fragLength) {
		this.fragLength = fragLength;
	}

	public short getAuthLength() {
		return authLength;
	}

	public void setAuthLength(short authLength) {
		this.authLength = authLength;
	}

	public int getCallId() {
		return callId;
	}

	public void setCallId(int callId) {
		this.callId = callId;
	}
	
	public boolean isPfcObjectUuid(){
		return (PFC_OBJECT_UUID & pfc_flags) == PFC_OBJECT_UUID;
	}
	
	@Override
	public String toString() {
		return new String("rpcVers = " + this.rpcVers+
				"rpcVersMinor = " +this.rpcVersMinor+
				"ptype = " + ptype +
				"pfc_flags = " + pfc_flags+
				"packedDrep = " +  packedDrep+
				"fragLength = " + fragLength+
				"authLength = " + authLength+
				"callID = " + callId);
	}
}
