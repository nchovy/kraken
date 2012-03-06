package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;

public class RpcSecVerificationTrailer {

	private byte []signature; // must be 8 byte
	// 0x8a , 0xe3 , 0x 13 , 0x71
	// 0x02 , 0xf4 , 0x36 , 0x71

	RpcSecVerificationTrailer(){
		signature = new byte[8];
	}
	public void parse(Buffer b){
		b.gets(signature);
	}
	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}
	
}
