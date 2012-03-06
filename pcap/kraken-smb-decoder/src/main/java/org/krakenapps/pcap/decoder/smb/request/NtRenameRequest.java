package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0xA5
public class NtRenameRequest implements SmbData{
	boolean malformed = false;
	//param
	byte wordCount;
	FileAttributes searchAttributes;
	short informationLevel;
	int reserved;
	//data
	short byteCount;
	byte bufferFormat1;
	String oldFileName;
	byte bufferFormat2;
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
	public short getInformationLevel() {
		return informationLevel;
	}
	public void setInformationLevel(short informationLevel) {
		this.informationLevel = informationLevel;
	}
	public int getReserved() {
		return reserved;
	}
	public void setReserved(int reserved) {
		this.reserved = reserved;
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
	public byte getBufferFormat2() {
		return bufferFormat2;
	}
	public void setBufferFormat2(byte bufferFormat2) {
		this.bufferFormat2 = bufferFormat2;
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
		return String.format("First Level : Nt Rename Request\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"FileAttributes = %s , informationLvel = 0x%s , reserved = 0x%s\n"+
				"byteCount = 0x%s\n"+
				"bufferFormat1 = 0x%s , oldFileName = %s\n"+
				"bufferFormat2 = 0x%s , newFileName = %s\n",
				this.malformed,
				this.wordCount,
				this.searchAttributes, this.informationLevel , this.reserved,
				this.bufferFormat1 , this.oldFileName,
				this.bufferFormat2 , this.newFileName);
	}
}
