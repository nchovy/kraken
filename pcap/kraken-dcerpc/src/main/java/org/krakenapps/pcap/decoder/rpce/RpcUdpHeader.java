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

import org.krakenapps.pcap.decoder.rpce.rr.UdpPDUType;
import org.krakenapps.pcap.decoder.rpce.structure.Uuid;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class RpcUdpHeader {
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
	// small = 1 byte
	// short = 2 byte
	// long = 4byte
	// hyper = 8byte
	private byte rpc_vers;
	private UdpPDUType pType;
	private byte flags1;
	private byte flags2;
	private byte []drep; //3bytes
	private byte serialHi;
	private Uuid object;
	private Uuid if_id;
	private Uuid act_id;
	private int serverBoot;
	private int ifVers;
	private int seqNum;
	private short opNum;
	private short iHint;
	private short aHint;
	private short len;
	private short fragNum;
	private byte authProto; // 0 : none, 1: OSF DCE Private Key Authentication
	private byte serialLo;
	public RpcUdpHeader() {
		drep = new byte[3];
		object = new Uuid();
		if_id = new Uuid();
		act_id = new Uuid();
	}
	public void parse(Buffer b){
		rpc_vers = b.get();
		pType = UdpPDUType.parse( b.get() & 0xff );
		flags1 = b.get();
		flags2 = b.get();
		b.gets(drep);
		serialHi = b.get();
		object.parse(b);
		if_id.parse(b);
		act_id.parse(b);
		serverBoot = ByteOrderConverter.swap(b.getInt());
		ifVers = ByteOrderConverter.swap(b.getInt());
		seqNum = ByteOrderConverter.swap(b.getInt());
		opNum = ByteOrderConverter.swap(b.getShort());
		iHint = ByteOrderConverter.swap(b.getShort());
		aHint = ByteOrderConverter.swap(b.getShort());
		len = ByteOrderConverter.swap(b.getShort());
		fragNum = ByteOrderConverter.swap(b.getShort());
		authProto = b.get();
		serialLo = b.get();
		
	}
	
	public UdpPDUType getpType() {
		return pType;
	}
	public void setpType(UdpPDUType pType) {
		this.pType = pType;
	}
	public byte getRpc_vers() {
		return rpc_vers;
	}
	public void setRpc_vers(byte rpc_vers) {
		this.rpc_vers = rpc_vers;
	}
	public byte getFlags1() {
		return flags1;
	}
	public void setFlags1(byte flags1) {
		this.flags1 = flags1;
	}
	public byte getFlags2() {
		return flags2;
	}
	public void setFlags2(byte flags2) {
		this.flags2 = flags2;
	}
	public byte[] getDrep() {
		return drep;
	}
	public void setDrep(byte[] drep) {
		this.drep = drep;
	}
	public byte getSerialHi() {
		return serialHi;
	}
	public void setSerialHi(byte serialHi) {
		this.serialHi = serialHi;
	}
	public Uuid getObject() {
		return object;
	}
	public void setObject(Uuid object) {
		this.object = object;
	}
	public Uuid getIf_id() {
		return if_id;
	}
	public void setIf_id(Uuid if_id) {
		this.if_id = if_id;
	}
	public Uuid getAct_id() {
		return act_id;
	}
	public void setAct_id(Uuid act_id) {
		this.act_id = act_id;
	}
	public int getServerBoot() {
		return serverBoot;
	}
	public void setServerBoot(int serverBoot) {
		this.serverBoot = serverBoot;
	}
	public int getIfVers() {
		return ifVers;
	}
	public void setIfVers(int ifVers) {
		this.ifVers = ifVers;
	}
	public int getSeqNum() {
		return seqNum;
	}
	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}
	public short getOpNum() {
		return opNum;
	}
	public void setOpNum(short opNum) {
		this.opNum = opNum;
	}
	public short getiHint() {
		return iHint;
	}
	public void setiHint(short iHint) {
		this.iHint = iHint;
	}
	public short getaHint() {
		return aHint;
	}
	public void setaHint(short aHint) {
		this.aHint = aHint;
	}
	public short getLen() {
		return len;
	}
	public void setLen(short len) {
		this.len = len;
	}
	public short getFragNum() {
		return fragNum;
	}
	public void setFragNum(short fragNum) {
		this.fragNum = fragNum;
	}
	public byte getAuthProto() {
		return authProto;
	}
	public void setAuthProto(byte authProto) {
		this.authProto = authProto;
	}
	public byte getSerialLo() {
		return serialLo;
	}
	public void setSerialLo(byte serialLo) {
		this.serialLo = serialLo;
	}
	@Override
	public String toString() {
		return new String("rpc_vers = "+ rpc_vers+
				"pType = " + pType +
				"flags1 = " + flags1+
				"flags2 = " + flags2+
				"serialHi = " +serialHi+
				"object = " + object+
				"if_id = " + if_id+
				"serverBoot = " + serverBoot
/*	private int ifVers;
	private int seqNum;
	private short opNum;
	private short iHint;
	private short aHint;
	private short len;
	private short fragNum;
	private byte authProto; // 0 : none, 1: OSF DCE Private Key Authentication
	private byte serialLo;*/);
	}
}
// dc_rpc_cl_pkt_hdr_t
// DCE RPC ConnectLess Packet Header Type