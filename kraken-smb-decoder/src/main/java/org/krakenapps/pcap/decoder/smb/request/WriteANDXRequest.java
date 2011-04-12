package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
// 0x2F
public class WriteANDXRequest implements SmbData{

	boolean malformed = false;
	//param
	byte wordCount;
	byte andxCommand;
	byte andxReserved;
	short andxOffset;
	short fid;
	int offset;
	int timeout;
	short writeMode;
	short remaining;
	short reserved; // when use in extension message , this field is used to datalengthHigh
	short dataLength;
	short dataOffset;
	int offsetHigh;

	//data
	short byteCount;
	byte pad;
	byte []Data; // new DataLength
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public byte getAndxCommand() {
		return andxCommand;
	}
	public void setAndxCommand(byte andxCommand) {
		this.andxCommand = andxCommand;
	}
	public byte getAndxReserved() {
		return andxReserved;
	}
	public void setAndxReserved(byte andxReserved) {
		this.andxReserved = andxReserved;
	}
	public short getAndxOffset() {
		return andxOffset;
	}
	public void setAndxOffset(short andxOffset) {
		this.andxOffset = andxOffset;
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
	public short getRemaining() {
		return remaining;
	}
	public void setRemaining(short remaining) {
		this.remaining = remaining;
	}
	public short getReserved() {
		return reserved;
	}
	public void setReserved(short reserved) {
		this.reserved = reserved;
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
	public byte getPad() {
		return pad;
	}
	public void setPad(byte pad) {
		this.pad = pad;
	}
	public byte[] getData() {
		return Data;
	}
	public void setData(byte[] data) {
		Data = data;
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
		return String.format("First Level : Write Andx Request\n" +
				"isMalformed = %s\n" +
				"wordcount = 0x%s\n" +
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0xs\n" +
				"fid = 0x%s , offset = 0x%s , timeOut = 0x%s\n" +
				"writeMode = 0x%s , remaining = 0x%s , reserved = 0x%s\n" +
				"dataLength = 0x%s , dataOffset = 0x%s , offsetHigh = 0x%s\n" +
				"byteCount = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.andxCommand), Integer.toHexString(this.andxReserved) , Integer.toHexString(this.andxOffset),
				Integer.toHexString(this.fid), Integer.toHexString(this.offset) , Integer.toHexString(this.timeout),
				Integer.toHexString(this.writeMode) , Integer.toHexString(this.remaining) , Integer.toHexString(this.reserved),
				Integer.toHexString(this.dataLength) , Integer.toHexString(this.dataOffset) , Integer.toHexString(this.offsetHigh),
				Integer.toHexString(this.byteCount));
	}
}
