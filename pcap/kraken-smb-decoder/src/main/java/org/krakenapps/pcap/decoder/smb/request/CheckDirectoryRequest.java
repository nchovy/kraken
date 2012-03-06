package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class CheckDirectoryRequest implements SmbData{


	byte wordCount;
	short byteCount;
	byte bufferFormat;
	String reqDirectoryname;
	boolean malformed = false;
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
	public String getReqDirectoryname() {
		return reqDirectoryname;
	}
	public void setReqDirectoryname(String reqDirectoryname) {
		this.reqDirectoryname = reqDirectoryname;
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
		return String.format("First Level : checkDirectory Request\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"byteCount = 0x%s\n"+
				"bufferFormat = 0x%s , reqDirectoryName = %s\n",
				this.isMalformed() ,
				Integer.toHexString(this.wordCount) ,
				Integer.toHexString(this.byteCount) ,
				Integer.toHexString(this.bufferFormat),this.reqDirectoryname);
	}
}
