package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
// 0x08
public class QueryInfoRequest implements SmbData{

	boolean malformed = false;
	//param
	byte wordCount;
	//data
	short byteCount;
	byte bufferFormat;
	String fileName;
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
	public byte getBufferFormat() {
		return bufferFormat;
	}
	public void setBufferFormat(byte bufferFormat) {
		this.bufferFormat = bufferFormat;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
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
		return String.format("First Level : Query Info Request\n" +
				"isMalforemd = %s\n" +
				"wordCount = 0x%s(it must 0x00)\n" +
				"byteCount = 0x%s\n" +
				"bufferFormat = 0x%s , fileName = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.bufferFormat) , this.fileName);
	}
}
