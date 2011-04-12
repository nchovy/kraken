package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x84
public class FindCloseResponse implements SmbData{
	boolean malformed = false;
	byte wordCount; // it must 0x00
	short count;
	short byteCount; // it must 0x0000
	byte bufferFormat;
	short dataLength; //must be 0x0000
	public short getCount() {
		return count;
	}
	public void setCount(short count) {
		this.count = count;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	public byte getBufferFormat() {
		return bufferFormat;
	}
	public void setBufferFormat(byte bufferFormat) {
		this.bufferFormat = bufferFormat;
	}
	public short getDataLength() {
		return dataLength;
	}
	public void setDataLength(short dataLength) {
		this.dataLength = dataLength;
	}
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
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
		return String.format("First Level : Find Close Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"count = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"bufferFormat = 0x%s , dataLength = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.count),
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.bufferFormat), Integer.toHexString(this.dataLength));
	}
}
