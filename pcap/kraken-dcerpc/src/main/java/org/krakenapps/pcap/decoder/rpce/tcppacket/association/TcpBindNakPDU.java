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
import org.krakenapps.pcap.decoder.rpce.structure.PRtVersionsSupported;
import org.krakenapps.pcap.decoder.rpce.structure.Uuid;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class TcpBindNakPDU implements TcpPDUInterface {

	public final byte REASON_NOT_SPECIFIED = 0x00;
	public final byte TEMPORARY_CONGESTION = 0x01;
	public final byte LOCAL_LIMIT_EXCEEDED = 0x02;
	public final byte CALLED_PADDR_UNKOWN = 0x03;
	public final byte PROTOCOL_VERSION_NOT_SUPPORTED = 0x04;
	public final byte DEFAULT_CONTEXT_NOT_SUPPORTED = 0x05;
	public final byte USER_DATA_NOT_READABLE = 0x06;
	public final byte NO_PSAP_AVAILABLE = 0x07;
	// added two reason
	public final byte AUTHENTICATION_TYPE_NOT_RECONGNIZE = 0x08;
	public final byte INVALID_CHECKSUM = 0x09;

	private int providerRejectReason;
	private PRtVersionsSupported version;
	// added optional field;
	private Uuid signature;

	public TcpBindNakPDU() {
		version = new PRtVersionsSupported();
		signature = new Uuid();
	}

	@Override
	public void parse(Buffer b, RpcTcpHeader h) {
		providerRejectReason = ByteOrderConverter.swap(b.getInt());
		version.parse(b);
		signature.parse(b);
	}

	public int getProviderRejectReason() {
		return providerRejectReason;
	}

	public void setProviderRejectReason(int providerRejectReason) {
		this.providerRejectReason = providerRejectReason;
	}

	public PRtVersionsSupported getVersion() {
		return version;
	}

	public void setVersion(PRtVersionsSupported version) {
		this.version = version;
	}

	public Uuid getSignature() {
		return signature;
	}

	public void setSignature(Uuid signature) {
		this.signature = signature;
	}

}
