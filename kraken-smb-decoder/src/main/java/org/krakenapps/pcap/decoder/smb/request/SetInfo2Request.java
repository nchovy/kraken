package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x22
public class SetInfo2Request implements SmbData{

	boolean malformed = false;
	//param
	byte wordCount;
	short fid;
	short createDate;
	short CreationTime;
	short lastAccessDate;
	short lastAccessTime;
	short lastWriteDate;
	short lastWriteTime;
	//data
	short byteCount;
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public short getCreateDate() {
		return createDate;
	}
	public void setCreateDate(short createDate) {
		this.createDate = createDate;
	}
	public short getCreationTime() {
		return CreationTime;
	}
	public void setCreationTime(short creationTime) {
		CreationTime = creationTime;
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
		return String.format("First Level : Set Info 2 Request\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s" +
				"fid = 0x%s , createDate = 0x%s , creationTime = 0x%s\n" +
				"lastAccessDate = 0x%s , lastAccessTime = 0x%s , lastWriteDate = 0x%s , lastWriteTime = 0x%s\n" +
				"byteCount = 0x%s(it Must 0x00)\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.fid), Integer.toHexString(this.createDate) , Integer.toHexString(this.CreationTime),
				Integer.toHexString(this.lastAccessDate) , Integer.toHexString(this.lastAccessTime) , Integer.toHexString(this.lastWriteDate) , Integer.toHexString(this.lastWriteTime),
				Integer.toHexString(this.byteCount));
	}
}
