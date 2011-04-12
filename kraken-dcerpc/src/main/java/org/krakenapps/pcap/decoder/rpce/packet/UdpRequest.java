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
