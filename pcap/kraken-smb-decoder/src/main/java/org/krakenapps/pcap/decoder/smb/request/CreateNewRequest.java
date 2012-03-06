package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.rr.*;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
public class CreateNewRequest implements SmbData{
	boolean malformed = false;
	//parameter
	byte WordCount;
	FileAttributes FileAttributes;
	int createionTime; 
	//data
	short byteCount;
	byte bufferFormat;
	String fileName;
	public byte getWordCount() {
		return WordCount;
	}
	public void setWordCount(byte wordCount) {
		WordCount = wordCount;
	}
	public FileAttributes getFileAttributes() {
		return FileAttributes;
	}
	public void setFileAttributes(FileAttributes fileAttributes) {
		FileAttributes = fileAttributes;
	}
	public int getCreateionTime() {
		return createionTime;
	}
	public void setCreateionTime(int createionTime) {
		this.createionTime = createionTime;
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
		return String.format("First Level : Create New Request\n"+
				"isMalforemd = %s\n" +
				"wordCount = 0x%s\n" +
				"fileAttributes =%s ,creationTime = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"bufferFormat = 0x%s , fileName = %s\n",
				this.malformed ,
				Integer.toHexString(this.WordCount) ,
				this.FileAttributes ,Integer.toHexString(this.createionTime) ,
				Integer.toHexString(this.byteCount) ,
				Integer.toHexString(this.bufferFormat),	this.fileName);
	}

}
