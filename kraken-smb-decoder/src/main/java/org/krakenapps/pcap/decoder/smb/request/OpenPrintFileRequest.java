package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0xC0
public class OpenPrintFileRequest implements SmbData{
	boolean malformed = false;
	//param
	byte wordCount;
	short setupLength;
	short mode;
	//data
	short byteCount;
	byte bufferFormat;
	String identifier;
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getSetupLength() {
		return setupLength;
	}
	public void setSetupLength(short setupLength) {
		this.setupLength = setupLength;
	}
	public short getMode() {
		return mode;
	}
	public void setMode(short mode) {
		this.mode = mode;
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
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
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
		return String.format("First Level : OpenPrintFile Request\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"setupLength = 0x%s, mode = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"bufferFormat = 0x%s , identifier = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.setupLength), Integer.toHexString(this.mode),
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.bufferFormat),this.identifier);
	}
}
