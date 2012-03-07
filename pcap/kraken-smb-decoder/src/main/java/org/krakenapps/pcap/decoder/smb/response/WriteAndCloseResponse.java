package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x2C
public class WriteAndCloseResponse implements SmbData{

	boolean malformed = false;
	byte wordCount; // it must 0x00
	short countOfBytesWritten;
	short byteCount; // it must 0x0000
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getCountOfBytesWritten() {
		return countOfBytesWritten;
	}
	public void setCountOfBytesWritten(short countOfBytesWritten) {
		this.countOfBytesWritten = countOfBytesWritten;
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
		return String.format("First Level : Write And Close Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"countOfByteWritten = 0x%s\n" +
				"byteCount = 0x%s\n(it must 0x00)",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.countOfBytesWritten),
				Integer.toHexString(this.byteCount));
	}
}
