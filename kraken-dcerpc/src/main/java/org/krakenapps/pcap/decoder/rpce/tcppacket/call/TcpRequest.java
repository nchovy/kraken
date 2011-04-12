package org.krakenapps.pcap.decoder.rpce.tcppacket.call;

import org.krakenapps.pcap.decoder.rpce.RpcTcpHeader;
import org.krakenapps.pcap.decoder.rpce.structure.AuthVerifierCo;
import org.krakenapps.pcap.decoder.rpce.structure.Uuid;
import org.krakenapps.pcap.decoder.rpce.tcppacket.association.TcpPDUInterface;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class TcpRequest implements TcpPDUInterface {

	private int allocHint;
	private short pContId;
	private short opNum;
	private Uuid object;
	private AuthVerifierCo authVerifier;

	public TcpRequest(){
		object = new Uuid();
		authVerifier = new AuthVerifierCo();
	}
	@Override
	public void parse(Buffer b, RpcTcpHeader h) {
		allocHint = ByteOrderConverter.swap(b.getInt());
		pContId = ByteOrderConverter.swap(b.getShort());
		opNum = ByteOrderConverter.swap(b.getShort());
		//System.out.println("allocHint = " + allocHint);
		//System.out.println("pContId = " + pContId);
		//System.out.println("opNum = " + opNum);
		if(h.isPfcObjectUuid() == true){
			object.parse(b);
		}
		//TODO: Uuid parsing & authVerifier check
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
