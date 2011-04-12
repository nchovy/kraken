package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
// command Code 0x00
public class CreateDirectoryRequest implements SmbData{
	boolean malformed = false;
	byte wordCount;	
	short byteCount;
	byte buffferFormat;
	String directoryName;
	public short getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte reqWordCount) {
		this.wordCount = reqWordCount;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short reqByteCount) {
		this.byteCount = reqByteCount;
	}
	public byte getBuffferFormat() {
		return buffferFormat;
	}
	public void setBuffferFormat(byte reqBuffferFormat) {
		this.buffferFormat = reqBuffferFormat;
	}
	public String getDirectoryName() {
		return directoryName;
	}
	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}
	@Override
	public boolean isMalformed() {
		return malformed;
	}
	@Override
	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}
	@Override
	public String toString(){
		return String.format("First Level : Create Directory Request\n"+
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"byteCount = 0x%s\n"+
				"bufferFormat = 0x%s , directoryName = 0x%s\n",
				this.isMalformed() ,
				Integer.toHexString(this.wordCount) ,
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.buffferFormat) , this.directoryName);
	}
}
