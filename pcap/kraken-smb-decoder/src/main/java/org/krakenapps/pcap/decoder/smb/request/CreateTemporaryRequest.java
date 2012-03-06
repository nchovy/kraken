package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class CreateTemporaryRequest implements SmbData{

	boolean malformed = false;
	//parameter
	byte wordCount;
	FileAttributes fileAttributes;
	int creationTime; 
	//data
	short byteCount;
	byte bufferFormat;
	String directoryName;
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public FileAttributes getFileAttributes() {
		return fileAttributes;
	}
	public void setFileAttributes(FileAttributes fileAttributes) {
		this.fileAttributes = fileAttributes;
	}
	public int getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(int creationTime) {
		this.creationTime = creationTime;
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
	public String getDirectoryName() {
		return directoryName;
	}
	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
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
		return String.format("First Level : Create Temporary Request\n"+
				"ismalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"fileAttributes = %s , creationTime = 0x%s\n"+
				"byteCount = 0x%s\n" +
				"bufferFormat = 0x%s , directoryName = %s",
				this.malformed,
				Integer.toHexString(this.wordCount),
				this.fileAttributes , Integer.toHexString(this.creationTime),
				Integer.toHexString(this.byteCount),
				this.bufferFormat,this.directoryName);
	}
}
