package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
// 0x0c
public class LockByteRangeRequest implements SmbData{

	boolean malformed = false;
	//parameter
	byte wordCount;
	short fid;
	int countOfBytesToLock;
	int lockOffsetInBytes; 
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
	public int getCountOfBytesToLock() {
		return countOfBytesToLock;
	}
	public void setCountOfBytesToLock(int countOfBytesToLock) {
		this.countOfBytesToLock = countOfBytesToLock;
	}
	public int getLockOffsetInBytes() {
		return lockOffsetInBytes;
	}
	public void setLockOffsetInBytes(int lockOffsetInBytes) {
		this.lockOffsetInBytes = lockOffsetInBytes;
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
		return String.format("First Level : Lock Byte Range Request\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"fid = 0x%s , countOfBytesToLock = 0x%s , lockOffsetInBytes = 0x%s\n"+
				"byteCount = 0x%s(it Must 0x00)",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.fid), Integer.toHexString(this.countOfBytesToLock) , Integer.toHexString(this.lockOffsetInBytes),
				Integer.toHexString(this.byteCount));
	}
}
