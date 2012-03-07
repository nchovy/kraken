package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x23
public class QueryInfo2Response implements SmbData{

	boolean malformed = false;
	byte wordCount;
	short createDate;
	short createTime;
	short lastAccessDate;
	short lastAccessTime;
	short lastWriteDate;
	short lastWriteTime;
	int fileDateSize;
	int fileAllocationSize;
	FileAttributes fileAttributes;
	short byteCount;
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getCreateDate() {
		return createDate;
	}
	public void setCreateDate(short createDate) {
		this.createDate = createDate;
	}
	public short getCreateTime() {
		return createTime;
	}
	public void setCreateTime(short createTime) {
		this.createTime = createTime;
	}
	public short getLastAccessDate() {
		return lastAccessDate;
	}
	public void setLastAccessDate(short lastAccessDate) {
		this.lastAccessDate = lastAccessDate;
	}
	public short getLastAccessTime() {
		return lastAccessTime;
	}
	public void setLastAccessTime(short lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}
	public short getLastWriteDate() {
		return lastWriteDate;
	}
	public void setLastWriteDate(short lastWriteDate) {
		this.lastWriteDate = lastWriteDate;
	}
	public short getLastWriteTime() {
		return lastWriteTime;
	}
	public void setLastWriteTime(short lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}
	public int getFileDateSize() {
		return fileDateSize;
	}
	public void setFileDateSize(int fileDateSize) {
		this.fileDateSize = fileDateSize;
	}
	public int getFileAllocationSize() {
		return fileAllocationSize;
	}
	public void setFileAllocationSize(int fileAllocationSize) {
		this.fileAllocationSize = fileAllocationSize;
	}
	public FileAttributes getFileAttributes() {
		return fileAttributes;
	}
	public void setFileAttributes(FileAttributes fileAttributes) {
		this.fileAttributes = fileAttributes;
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
		return String.format("First Level : Query Info 2 Response\n" +
				"isMalformed %s\n" +
				"wordCount = 0x%s\n" +
				"createDate = 0x%s , createTime = 0x%s , lastAccessDate = 0x%s\n" +
				"lastAccessTime = 0x%s,  lastWriteDate = 0x%s,  lastWriteTime = 0x%s\n" +
				"fileDataSize = 0x%s , fileAllocationSize = 0x%s , FileAttributes = %s\n" +
				"byteCount = 0x%s(it must 0x00)\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.createDate), Integer.toHexString(this.createTime) , Integer.toHexString(this.lastAccessDate),
				Integer.toHexString(this.lastAccessTime) , Integer.toHexString(this.lastWriteDate) , Integer.toHexString(this.lastWriteTime),
				Integer.toHexString(this.fileDateSize) , Integer.toHexString(this.fileAllocationSize) , this.fileAttributes,
				Integer.toHexString(this.byteCount));
	}
}
