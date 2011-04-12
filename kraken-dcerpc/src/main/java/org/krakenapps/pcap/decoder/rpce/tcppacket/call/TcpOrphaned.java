package org.krakenapps.pcap.decoder.rpce.tcppacket.call;

import org.krakenapps.pcap.decoder.rpce.RpcTcpHeader;
import org.krakenapps.pcap.decoder.rpce.structure.AuthVerifierCo;
import org.krakenapps.pcap.decoder.rpce.tcppacket.association.TcpPDUInterface;
import org.krakenapps.pcap.util.Buffer;

public class TcpOrphaned implements TcpPDUInterface{

	AuthVerifierCo authVerifier;
	public TcpOrphaned(){
		authVerifier = new AuthVerifierCo();
	}

	public AuthVerifierCo getAuthVerifier() {
		return authVerifier;
	}

	public void setAuthVerifier(AuthVerifierCo authVerifier) {
		this.authVerifier = authVerifier;
	}

	@Override
	public void parse(Buffer b, RpcTcpHeader h) {
		if(h.getAuthLength() != 0){
			authVerifier.parse(b);
		}
	}
	
}
