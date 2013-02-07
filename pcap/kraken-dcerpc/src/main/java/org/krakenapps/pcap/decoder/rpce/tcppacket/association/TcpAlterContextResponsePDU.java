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

public class TcpAlterContextResponsePDU implements TcpPDUInterface {

	private short maxXmitFrag;
	private short maxRecvFrag;
	private int assocGroupId;
	private PortAny secAddr;
	private byte[] pad;
	private PResultList resultList;
	private AuthVerifierCo authVerifier;

	public TcpAlterContextResponsePDU() {
		secAddr = new PortAny();
		resultList = new PResultList();
		authVerifier = new AuthVerifierCo();
	}

	@Override
	public void parse(Buffer b, RpcTcpHeader h) {
		int length = 8;
		maxXmitFrag = ByteOrderConverter.swap(b.getShort());
		maxRecvFrag = ByteOrderConverter.swap(b.getShort());
		assocGroupId = ByteOrderConverter.swap(b.getInt());
		secAddr.parse(b);
		length = length + secAddr.getLength();
		length = length % 4;
		if (length != 0) {
			pad = new byte[length];
			b.gets(pad);
		}
		resultList.parse(b);
		if(h.getAuthLength() != 0){
			authVerifier.parse(b);
		}
	}

	public byte[] getPad() {
		return pad;
	}

	public void setPad(byte[] pad) {
		this.pad = pad;
	}

	public PResultList getResultList() {
		return resultList;
	}

	public void setResultList(PResultList resultList) {
		this.resultList = resultList;
	}

	public AuthVerifierCo getAuthVerifier() {
		return authVerifier;
	}

	public void setAuthVerifier(AuthVerifierCo authVerifier) {
		this.authVerifier = authVerifier;
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
}
