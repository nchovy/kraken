package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class UnlockByteRangeRequest implements SmbData{

	boolean malformed = false;
	byte wordCount;
	short fid;
	int countOfBytesToLock;
	int unLockOffsetInBytes; 
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
	public int getunLockOffsetInBytes() {
		return unLockOffsetInBytes;
	}
	public void setUnLockOffsetInBytes(int lockOffsetInBytes) {
		this.unLockOffsetInBytes = lockOffsetInBytes;
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
		return String.format("First Level : Unlock Byte Range Request\n" +
				"isMalformed = %s\n" +
				"wordCoutn = 0x%s\n" +
				"fid = 0x%s , countOfBytesToLock = 0x%s , unLockOffsetInBytes = 0x%s\n" +
				"byteCount = 0x%s(it must 0x00)\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.fid) , Integer.toHexString(this.countOfBytesToLock) , Integer.toHexString(this.unLockOffsetInBytes),	
				Integer.toHexString(this.byteCount));
	}
}
