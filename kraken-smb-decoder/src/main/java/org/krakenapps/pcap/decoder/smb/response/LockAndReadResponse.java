package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class LockAndReadResponse implements SmbData{

	boolean malformed = false;
	byte wordCount;
	short countofBytesReturned;
	byte []reserved ;
	short byteCount;
	byte bufferType;
	short countOfBytesRead;
	byte []bytes;
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getCountofBytesReturned() {
		return countofBytesReturned;
	}
	public void setCountofBytesReturned(short countofBytesReturned) {
		this.countofBytesReturned = countofBytesReturned;
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
	public byte getBufferType() {
		return bufferType;
	}
	public void setBufferType(byte bufferType) {
		this.bufferType = bufferType;
	}
	public short getCountOfBytesRead() {
		return countOfBytesRead;
	}
	public void setCountOfBytesRead(short countOfBytesRead) {
		this.countOfBytesRead = countOfBytesRead;
	}
	public byte[] getBytes() {
		return bytes;
	}
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
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
		return String.format("First Level : Lock And Read Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"countOfBytesReturn = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"bufferType = 0x%s , countOfBytesRead = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.countofBytesReturned),
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.bufferType) , Integer.toHexString(this.countOfBytesRead));
	}
}
