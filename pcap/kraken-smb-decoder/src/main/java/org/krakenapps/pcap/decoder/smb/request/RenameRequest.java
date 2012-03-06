package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class RenameRequest implements SmbData{

	//param
	boolean malformed = false;
	byte wordCount;
	FileAttributes searchAttributes;
	//data
	short byteCount;
	byte bufferFormat1;
	String oldFileName;
	byte bufferCormat2;
	String newFileName;
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public FileAttributes getSearchAttributes() {
		return searchAttributes;
	}
	public void setSearchAttributes(FileAttributes searchAttributes) {
		this.searchAttributes = searchAttributes;
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
	public String getOldFileName() {
		return oldFileName;
	}
	public void setOldFileName(String oldFileName) {
		this.oldFileName = oldFileName;
	}
	public byte getReqBufferCormat2() {
		return bufferCormat2;
	}
	public void setBufferFormat2(byte bufferCormat2) {
		this.bufferCormat2 = bufferCormat2;
	}
	public String getNewFileName() {
		return newFileName;
	}
	public void setNewFileName(String newFileName) {
		this.newFileName = newFileName;
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
		return String.format("First Level : Rename Request\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"searchAttributes = %s\n" +
				"bytecount = 0x%s\n" +
				"bufferFormat1 = 0x%s , oldFileName = %s\n" +
				"bufferFormat2 = 0x%s , newFileName = %s\n" ,
				this.malformed,
				Integer.toHexString(this.wordCount),
				this.searchAttributes,
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.bufferFormat1),this.oldFileName,
				Integer.toHexString(this.bufferCormat2) , this.newFileName);
	}
}
