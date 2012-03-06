package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class SetInfoRequest implements SmbData{

	boolean malformed = false;
	//param
	byte wordCount;
	FileAttributes fileAttributes;
	int lastWriteTime;
	byte []reserved = new byte[10];
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
	public FileAttributes getFileAttributes() {
		return fileAttributes;
	}
	public void setFileAttributes(FileAttributes fileAttributes) {
		this.fileAttributes = fileAttributes;
	}
	public int getLastWriteTime() {
		return lastWriteTime;
	}
	public void setLastWriteTime(int lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}
	public byte[] getReserved() {
		return reserved;
	}
	public void setReserved(byte[] reserved) {
		this.reserved = reserved;
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
		return String.format("First Level : Set Info Request\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"fileAttributes = %s , lastWriteTime = 0x%s , reserved = %s\n" +
				"byteCount = 0x%s\n" +
				"bufferFormat = 0x%s , fileName = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				this.fileAttributes, Integer.toHexString(this.lastWriteTime) , this.reserved.toString(),
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.bufferFormat) , this.fileName);
	}
}
