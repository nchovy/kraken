package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x70
public class TreeConnectResponse implements SmbData{

	boolean malformed = false;
	byte wordCount; // 
	short maxBufferSize;
	short tid;
	short byteCount; // 
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getMaxBufferSize() {
		return maxBufferSize;
	}
	public void setMaxBufferSize(short maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
	}
	public short getTid() {
		return tid;
	}
	public void setTid(short tid) {
		this.tid = tid;
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
		return String.format("First Level : Tree Connect Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"maximalBufferSize = 0x%s, tid = 0x%s\n" +
				"byteCount = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.maxBufferSize) , Integer.toHexString(this.tid),
				Integer.toHexString(this.byteCount));
	}
}
