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
package org.krakenapps.pcap.decoder.rpce.tcppacket.association;

import org.krakenapps.pcap.decoder.rpce.RpcTcpHeader;
import org.krakenapps.pcap.decoder.rpce.structure.AuthVerifierCo;
import org.krakenapps.pcap.decoder.rpce.structure.PResultList;
import org.krakenapps.pcap.decoder.rpce.structure.PortAny;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class TcpBindAckPDU implements TcpPDUInterface {

	private short maxXmitFrag;
	private short maxRecvFrag;
	private int assocGroupId;
	private PortAny secAddr;
	private byte[] pad2;
	private PResultList pResultList;
	private AuthVerifierCo authVerifier;

	public TcpBindAckPDU() {
		secAddr = new PortAny();
		pResultList = new PResultList();
		authVerifier = new AuthVerifierCo();
	}

	@Override
	public void parse(Buffer b, RpcTcpHeader h) {
		int length = 0;
		maxXmitFrag = ByteOrderConverter.swap(b.getShort());
		maxRecvFrag = ByteOrderConverter.swap(b.getShort());
		assocGroupId = ByteOrderConverter.swap(b.getInt());
		secAddr.parse(b);
		secAddr.parse(b);
		length = length + secAddr.getLength();
		length = length % 4;
		if (length != 0) {
			pad2 = new byte[length];
			b.gets(pad2);
		}
		pResultList.parse(b);
		if(h.getAuthLength() != 0){
			authVerifier.parse(b);
		}
	}

	public short getMaxXmitFrag() {
		return maxXmitFrag;
	}

	public void setMaxXmitFrag(short maxXmitFrag) {
		this.maxXmitFrag = maxXmitFrag;
	}

	public short getMaxRecvFrag() {
		return maxRecvFrag;
	}

	public void setMaxRecvFrag(short maxRecvFrag) {
		this.maxRecvFrag = maxRecvFrag;
	}

	public int getAssocGroupId() {
		return assocGroupId;
	}

	public void setAssocGroupId(int assocGroupId) {
		this.assocGroupId = assocGroupId;
	}

	public PortAny getSecAddr() {
		return secAddr;
	}

	public void setSecAddr(PortAny secAddr) {
		this.secAddr = secAddr;
	}

	public byte[] getPad2() {
		return pad2;
	}

	public void setPad2(byte[] pad2) {
		this.pad2 = pad2;
	}

	public PResultList getpResultList() {
		return pResultList;
	}

	public void setpResultList(PResultList pResultList) {
		this.pResultList = pResultList;
	}

	public AuthVerifierCo getAuthVerifier() {
		return authVerifier;
	}

	public void setAuthVerifier(AuthVerifierCo authVerifier) {
		this.authVerifier = authVerifier;
	}
}
