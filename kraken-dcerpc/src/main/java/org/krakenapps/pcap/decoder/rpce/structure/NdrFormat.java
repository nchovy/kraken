package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;

public class NdrFormat {

	private byte intRep;
	private byte charRep;
	private byte floatRep;
	private byte reserved;
	public void parse(Buffer b){
		intRep = b.get();
		charRep = b.get();
		floatRep = b.get();
		reserved = b.get();
	}
	public byte getIntRep() {
		return intRep;
	}
	public void setIntRep(byte intRep) {
		this.intRep = intRep;
	}
	public byte getCharRep() {
		return charRep;
	}
	public void setCharRep(byte charRep) {
		this.charRep = charRep;
	}
	public byte getFloatRep() {
		return floatRep;
	}
	public void setFloatRep(byte floatRep) {
		this.floatRep = floatRep;
	}
	public byte getReserved() {
		return reserved;
	}
	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}
}
