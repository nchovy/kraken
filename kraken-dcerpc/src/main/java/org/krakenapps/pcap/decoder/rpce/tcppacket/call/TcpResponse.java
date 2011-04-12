package org.krakenapps.pcap.decoder.rpce.tcppacket.call;

import org.krakenapps.pcap.decoder.rpce.RpcTcpHeader;
import org.krakenapps.pcap.decoder.rpce.structure.AuthVerifierCo;
import org.krakenapps.pcap.decoder.rpce.tcppacket.association.TcpPDUInterface;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class TcpResponse implements TcpPDUInterface {

	private int allocHint;
	private short pContId;
	private byte cancelCount;
	private byte reserved;
	private AuthVerifierCo authVerifier;

	public TcpResponse() {
		authVerifier = new AuthVerifierCo();
	}

	@Override
	public void parse(Buffer b , RpcTcpHeader h) {
		allocHint = ByteOrderConverter.swap(b.getInt());
		pContId = ByteOrderConverter.swap(b.getShort());
		cancelCount = b.get();
		reserved = b.get();
		if(h.getAuthLength() != 0){
			authVerifier.parse(b);
		}
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
