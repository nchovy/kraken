package org.krakenapps.pcap.decoder.smb.request;

import org.krakenapps.pcap.decoder.smb.structure.SmbData;

// command code 0x01
public class DeleteDirectoryRequest implements SmbData {
	boolean malformed = false;
	// Parameter
	byte wordCount;
	// Data
	short byteCount;
	short bufferFormat;
	String directoryName;

	public byte getWordCount() {
		return wordCount;
	}

	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}

	public short getByteCount() {
		return byteCount;
	}

	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}

	public short getBufferFormat() {
		return bufferFormat;
	}

	public void setBufferFormat(short bufferFormat) {
		this.bufferFormat = bufferFormat;
	}

	public String getDirectoryName() {
		return directoryName;
	}

	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}
	@Override
	public boolean isMalformed() {
		// TODO Auto-generated method stub
		return malformed;
	}
	@Override
	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}
	@Override
	public String toString(){
		return String.format("First Level : Delete Directory request\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"byteCount = 0x%s\n"+
				"bufferFormat = 0x%s , directoryName = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.bufferFormat) , this.directoryName);
	}
}
