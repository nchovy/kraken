package org.krakenapps.pcap.decoder.smb.transresp;

import org.krakenapps.pcap.decoder.smb.TransData;

public class PeekNmpipeResponse implements TransData{
	
	short readDataAvailable;
	short messageBytesLength;
	short namedPipeState;
	
	byte[]readData;

	public short getReadDataAvailable() {
		return readDataAvailable;
	}

	public void setReadDataAvailable(short readDataAvailable) {
		this.readDataAvailable = readDataAvailable;
	}

	public short getMessageBytesLength() {
		return messageBytesLength;
	}

	public void setMessageBytesLength(short messageBytesLength) {
		this.messageBytesLength = messageBytesLength;
	}

	public short getNamedPipeState() {
		return namedPipeState;
	}

	public void setNamedPipeState(short namedPipeState) {
		this.namedPipeState = namedPipeState;
	}

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
