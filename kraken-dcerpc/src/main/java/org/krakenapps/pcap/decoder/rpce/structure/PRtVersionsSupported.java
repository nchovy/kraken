package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;


public class PRtVersionsSupported {

	byte nProtocols;
	Version []pProtocols;// size = n_protocols;
	public void parse(Buffer b){
		nProtocols = b.get();
		pProtocols = new Version[nProtocols];
		for(int i=0;i<nProtocols;i++){
			pProtocols[i] = new Version();
			pProtocols[i].parse(b);
		}
	}
	public byte getnProtocols() {
		return nProtocols;
	}
	public void setnProtocols(byte nProtocols) {
		this.nProtocols = nProtocols;
	}
	public Version[] getpProtocols() {
		return pProtocols;
	}
	public void setpProtocols(Version[] pProtocols) {
		this.pProtocols = pProtocols;
	}

	
}
