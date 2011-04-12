package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;

public class Version {
	byte major;
	byte minor;
	public void parse(Buffer b){
		major = b.get();
		minor = b.get();
	}
	public byte getMajor() {
		return major;
	}
	public void setMajor(byte major) {
		this.major = major;
	}
	public byte getMinor() {
		return minor;
	}
	public void setMinor(byte minor) {
		this.minor = minor;
	}
	
}
