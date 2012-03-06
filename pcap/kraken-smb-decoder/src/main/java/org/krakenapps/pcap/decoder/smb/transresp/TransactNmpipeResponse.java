package org.krakenapps.pcap.decoder.smb.transresp;

import org.krakenapps.pcap.decoder.smb.TransData;

public class TransactNmpipeResponse implements TransData{
	byte []readData;

	public byte[] getReadData() {
		return readData;
	}

	public void setReadData(byte[] readData) {
		this.readData = readData;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
