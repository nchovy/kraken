package org.krakenapps.pcap.decoder.smb.response;

import org.krakenapps.pcap.decoder.smb.structure.SmbData;

//0x2B
public class EchoResponse implements SmbData {
	boolean malformed = false;
	byte wordCount; // it must 0x00
	short sequenceNumber;
	short byteCount; // it must 0x0000
	byte[] Data; // new ByteCount;

	public byte getWordCount() {
		return wordCount;
	}

	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}

	public short getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(short sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public short getByteCount() {
		return byteCount;
	}

	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}

	public byte[] getData() {
		return Data;
	}

	public void setData(byte[] data) {
		Data = data;
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
	public String toString() {
		return String.format("First Level : Echo Response\n" +
				"isMalformed = %s\n" +
				"wordCount = %s\n" +
				"sequenceNumber = 0x%s\n" +
				"byteCount = 0x%s",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.sequenceNumber),
				Integer.toHexString(this.byteCount));
	}
}
