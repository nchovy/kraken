package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class WriteMPXRequest implements SmbData{

	boolean malformed = false;
	byte wordCount;
	short fid;
	short totalByteCount;
	short reserved;
	int byteOffsetToBeginwrite;
	int timeout;
	short writeMode;
	int reqMask;
	short dataLength;
	short dataOffset;
	//data
	short byteCount;
	byte []pad;
	byte []buffer;
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
	public short getTotalByteCount() {
		return totalByteCount;
	}
	public void setTotalByteCount(short totlaByteCount) {
		this.totalByteCount = totlaByteCount;
	}
	public short getReserved() {
		return reserved;
	}
	public void setReserved(short reserved) {
		this.reserved = reserved;
	}
	public int getByteOffsetToBeginwrite() {
		return byteOffsetToBeginwrite;
	}
	public void setByteOffsetToBeginwrite(int byteOffsetToBeginwrite) {
		this.byteOffsetToBeginwrite = byteOffsetToBeginwrite;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public short getWriteMode() {
		return writeMode;
	}
	public void setWriteMode(short writeMode) {
		this.writeMode = writeMode;
	}
	public int getReqMask() {
		return reqMask;
	}
	public void setReqMask(int reqMask) {
		this.reqMask = reqMask;
	}
	public short getDataLength() {
		return dataLength;
	}
	public void setDataLength(short dataLength) {
		this.dataLength = dataLength;
	}
	public short getDataOffset() {
		return dataOffset;
	}
	public void setDataOffset(short dataOffset) {
		this.dataOffset = dataOffset;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	public byte[] getPad() {
		return pad;
	}
	public void setPad(byte[] pad) {
		this.pad = pad;
	}
	public byte[] getBuffer() {
		return buffer;
	}
	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
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
		return String.format("First Level : Write MPX Request\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"fid = 0x%s , totalByteCount = 0x%s , reserved = 0x%s\n" +
				"byteoffsetToBeginwrite = 0x%s , timeOut = 0x%s , writeMode = 0x%s\n" +
				"reqMask = 0x%s , dataLength = 0x%s , dataOffset = 0x%s\n" +
				"byteCount = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.fid), Integer.toHexString(this.totalByteCount) , Integer.toHexString(this.reserved),
				Integer.toHexString(this.byteOffsetToBeginwrite) , Integer.toHexString(this.timeout) , Integer.toHexString(this.writeMode),
				Integer.toHexString(this.reqMask) , Integer.toHexString(this.dataLength) , Integer.toHexString(this.dataOffset),
				Integer.toHexString(this.byteCount));
	}
}
