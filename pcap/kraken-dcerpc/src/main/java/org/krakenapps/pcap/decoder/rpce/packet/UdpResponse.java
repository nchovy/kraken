package org.krakenapps.pcap.decoder.rpce.packet;

import org.krakenapps.pcap.decoder.rpce.RpcUdpHeader;
import org.krakenapps.pcap.decoder.rpce.structure.AuthVerifierCo;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class UdpResponse implements UdpPDUInterface {

	private int allocHint;
	private short pContId;
	private byte cancelCount;
	private byte reserved;
	private AuthVerifierCo authVerifier;

	public UdpResponse() {
		authVerifier = new AuthVerifierCo();
	}

	@Override
	public void parse(Buffer b , RpcUdpHeader h) {
		allocHint = ByteOrderConverter.swap(b.getInt());
		pContId = ByteOrderConverter.swap(b.getShort());
		cancelCount = b.get();
		reserved = b.get();
		authVerifier.parse(b);
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

	public byte getCancelCount() {
		return cancelCount;
	}

	public void setCancelCount(byte cancelCount) {
		this.cancelCount = cancelCount;
	}

	public byte getReserved() {
		return reserved;
	}

	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}

	public AuthVerifierCo getAuthVerifier() {
		return authVerifier;
	}

	public void setAuthVerifier(AuthVerifierCo authVerifier) {
		this.authVerifier = authVerifier;
	}

}
