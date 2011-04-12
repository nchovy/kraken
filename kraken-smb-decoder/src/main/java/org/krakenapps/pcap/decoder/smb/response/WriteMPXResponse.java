package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class WriteMPXResponse implements SmbData{
	
	boolean malformed = false;
	byte wordCount;
	int resMask;
	//data
	short byteCount;
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public int getResMask() {
		return resMask;
	}
	public void setResMask(int resMask) {
		this.resMask = resMask;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
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
		return String.format("First Level : Write MPX Response\n" +
				"wordCount = 0x%s\n" +
				"resMask = 0x%s\n" +
				"byteCount = 0x%s(it must 0x00)\n",
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.resMask),
				Integer.toHexString(this.byteCount));
	}
}

