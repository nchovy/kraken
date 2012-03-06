package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class WriteRawRequest implements SmbData{

	boolean malformed = false;
	//param
	byte wordCount;
	short fid;
	short countOfBytes;
	short reserved1;
	int offset;
	int timeout;
	short writeMode;
	short reserved2;
	int dataLength;
	short DataOffset;
	int offsetHigh; //(optional)
	//data
	short byteCount;
	byte []pad;
	byte []data;
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
	public short getCountOfBytes() {
		return countOfBytes;
	}
	public void setCountOfBytes(short contOfBytes) {
		this.countOfBytes = contOfBytes;
	}
	public short getReserved1() {
		return reserved1;
	}
	public void setReserved1(short reserved1) {
		this.reserved1 = reserved1;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
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
	public short getReserved2() {
		return reserved2;
	}
	public void setReserved2(short reserved2) {
		this.reserved2 = reserved2;
	}
	public int getDataLength() {
		return dataLength;
	}
	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}
	public short getDataOffset() {
		return DataOffset;
	}
	public void setDataOffset(short dataOffset) {
		DataOffset = dataOffset;
	}
	public int getOffsetHigh() {
		return offsetHigh;
	}
	public void setOffsetHigh(int offsetHigh) {
		this.offsetHigh = offsetHigh;
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
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
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
		return String.format("First Level : Write Raw Request\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"fid = 0x%s , countOfBytes = 0x%s , reserved1 = 0x%s\n" +
				"offset = 0x%s , timeOut = 0x%s , writeMode = 0x%s\n" +
				"reserved2 = 0x%s , dataLength = 0x%s , offsetHigh = 0x%s\n" +
				"byteCount = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.fid) , Integer.toHexString(this.countOfBytes) , Integer.toHexString(this.reserved1),
				Integer.toHexString(this.offset) , Integer.toHexString(this.timeout) , Integer.toHexString(this.writeMode),
				Integer.toHexString(this.reserved2) , Integer.toHexString(this.dataLength) , Integer.toHexString(this.offsetHigh),
				Integer.toHexString(this.byteCount));
	}
}
