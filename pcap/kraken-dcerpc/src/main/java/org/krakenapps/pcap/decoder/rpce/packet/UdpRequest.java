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
import org.krakenapps.pcap.decoder.rpce.packet.UdpPDUInterface;
import org.krakenapps.pcap.decoder.rpce.structure.AuthVerifierCo;
import org.krakenapps.pcap.decoder.rpce.structure.Uuid;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class UdpRequest implements UdpPDUInterface {

	private int allocHint;
	private short pContId;
	private short opNum;
	private Uuid object;
	private AuthVerifierCo authVerifier;

	public UdpRequest() {
		object = new Uuid();
		authVerifier = new AuthVerifierCo();
	}

	@Override
	public void parse(Buffer b, RpcUdpHeader h) {
		allocHint = ByteOrderConverter.swap(b.getInt());
		pContId = ByteOrderConverter.swap(b.getShort());
		opNum = ByteOrderConverter.swap(b.getShort());
		object.parse(b);
		// TODO: Uuid parsing & authVerifier check
		// authVerifier.parse(b);
	}

	public int getAllocHint() {
		return allocHint;
	}

	public void setAllocHint(int allocHint) {
		this.allocHint = allocHint;
	}

	public short getpContId() {
		return pContId;
	}

	public void setpContId(short pContId) {
		this.pContId = pContId;
	}

	public short getOpNum() {
		return opNum;
	}

	public void setOpNum(short opNum) {
		this.opNum = opNum;
	}

	public Uuid getObject() {
		return object;
	}

	public void setObject(Uuid object) {
		this.object = object;
	}

	public AuthVerifierCo getAuthVerifier() {
		return authVerifier;
	}

	public void setAuthVerifier(AuthVerifierCo authVerifier) {
		this.authVerifier = authVerifier;
	}
	// stub_data_length = frag_length - fixed Header Length - authLength;
}
