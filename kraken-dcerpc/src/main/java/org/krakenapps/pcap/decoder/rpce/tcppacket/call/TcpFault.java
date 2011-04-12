package org.krakenapps.pcap.decoder.rpce.tcppacket.call;

import org.krakenapps.pcap.decoder.rpce.RpcTcpHeader;
import org.krakenapps.pcap.decoder.rpce.structure.AuthVerifierCo;
import org.krakenapps.pcap.decoder.rpce.tcppacket.association.TcpPDUInterface;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class TcpFault implements TcpPDUInterface {

	private int allochint;
	private short pContId;
	private byte cancelCount;
	private byte reserved;
	private int status;
	private byte[] reserved2; // 4 byte
	private AuthVerifierCo authVerifier;

	public TcpFault() {
		authVerifier = new AuthVerifierCo();
		reserved2 = new byte[4];
	}

	@Override
	public void parse(Buffer b, RpcTcpHeader h) {
		allochint = ByteOrderConverter.swap(b.getInt());
		pContId = ByteOrderConverter.swap(b.getShort());
		cancelCount = b.get();
		reserved = b.get();
		status = ByteOrderConverter.swap(b.getInt());
		b.gets(reserved2);
		if(h.getAuthLength() != 0){
			authVerifier.parse(b);
		}
	}

	public int getAllochint() {
		return allochint;
	}

	public void setAllochint(int allochint) {
		this.allochint = allochint;
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public byte[] getReserved2() {
		return reserved2;
	}

	public void setReserved2(byte[] reserved2) {
		this.reserved2 = reserved2;
	}

	public AuthVerifierCo getAuthVerifier() {
		return authVerifier;
	}

	public void setAuthVerifier(AuthVerifierCo authVerifier) {
		this.authVerifier = authVerifier;
	}
}
