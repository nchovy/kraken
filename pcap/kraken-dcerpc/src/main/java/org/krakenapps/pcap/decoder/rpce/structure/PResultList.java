package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;

public class PResultList {

	byte nResults;
	byte reserved;
	short reserved2;
	PResult []results; // size of nResults
	
	public void parse(Buffer b){
		nResults = b.get();
		reserved = b.get();
		reserved2 = b.getShort();
		results = new PResult[nResults];
		for(int i=0;i<nResults;i++){
			results[i] = new PResult();
			results[i].parse(b);
		}
	}
	
	public byte getnResults() {
		return nResults;
	}
	public void setnResults(byte nResults) {
		this.nResults = nResults;
	}
	public byte getReserved() {
		return reserved;
	}
	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}
	public short getReserved2() {
		return reserved2;
	}
	public void setReserved2(short reserved2) {
		this.reserved2 = reserved2;
	}
	public PResult[] getResults() {
		return results;
	}
	public void setResults(PResult[] results) {
		this.results = results;
	}
	
}
