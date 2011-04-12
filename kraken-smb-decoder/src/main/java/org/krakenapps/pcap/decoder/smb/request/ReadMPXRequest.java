package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class ReadMPXRequest implements SmbData{

	boolean malformed = false;
	//param
	byte wordCount;
	short fid;
	int offset;
	short maxCountOfBytesToReturn;
	short minCountOfBytesToReturn;
	int timeout;
	short reserved;
	//data
	int byteCount;
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
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public short getMaxCountOfBytesToReturn() {
		return maxCountOfBytesToReturn;
	}
	public void setMaxCountOfBytesToReturn(short maxCountOfBytesToReturn) {
		this.maxCountOfBytesToReturn = maxCountOfBytesToReturn;
	}
	public short getMinCountOfBytesToReturn() {
		return minCountOfBytesToReturn;
	}
	public void setMinCountOfBytesToReturn(short minCountOfBytesToReturn) {
		this.minCountOfBytesToReturn = minCountOfBytesToReturn;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public short getReserved() {
		return reserved;
	}
	public void setReserved(short reserved) {
		this.reserved = reserved;
	}
	public int getByteCount() {
		return byteCount;
	}
	public void setByteCount(int byteCount) {
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
		return String.format("First Level : Read MPX Request\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"fid = 0x%s , offset = 0x%s , maxCountOfBytesToReturn = 0x%s\n" +
				"minCountofBytesToReturn = 0x%s , timeOut = 0x%s,  reserved = 0x%s\n" +
				"byteCount = 0x%s(it must 0x00)\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.fid) , Integer.toHexString(this.offset) , Integer.toHexString(this.maxCountOfBytesToReturn),
				Integer.toHexString(this.minCountOfBytesToReturn) , Integer.toHexString(this.timeout) , Integer.toHexString(this.reserved),
				Integer.toHexString(this.byteCount));
	}
}
