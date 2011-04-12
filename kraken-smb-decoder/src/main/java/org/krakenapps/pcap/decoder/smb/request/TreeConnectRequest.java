package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x70
public class TreeConnectRequest implements SmbData{

	boolean malformed = false;
	//param
	byte wordCount;
	//data
	short byteCount;
	byte bufferFormat1;
	String path;
	byte bufferFormat2;
	String password;
	byte bufferFormat3;
	String service;
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
	public byte getBufferFormat1() {
		return bufferFormat1;
	}
	public void setBufferFormat1(byte bufferFormat1) {
		this.bufferFormat1 = bufferFormat1;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public byte getBufferFormat2() {
		return bufferFormat2;
	}
	public void setBufferFormat2(byte bufferFormat2) {
		this.bufferFormat2 = bufferFormat2;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public byte getBufferFormat3() {
		return bufferFormat3;
	}
	public void setBufferFormat3(byte bufferFormat3) {
		this.bufferFormat3 = bufferFormat3;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
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
		return String.format("First Level : Tree Connect Request\n" +
				"isMalformed = %s\n"+
				"wordCount = 0x%s(it must 0x00)\n" +
				"byteCount = 0x%s\n" +
				"bufferFormat1 = 0x%s , path = %s\n" +
				"bufferFormat2 = 0x%s , password = %s\n" +
				"bufferformat3 = 0x%s , service = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.bufferFormat1), this.path,
				Integer.toHexString(this.bufferFormat2) , this.password,
				Integer.toHexString(this.bufferFormat3) , this.service);
	}
}
