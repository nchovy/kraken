package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class ReadResponse implements SmbData{
	boolean malformed = false;
//	ByteBuffer buffer;
	byte wordCount;
	short CountOfBytesReturned;
	byte []reserved = new byte[8];
	short byteCount;
	byte bufferFormat;
	short CountOfBytesRead;
	byte []bytes;// new resCountofBytesRead..
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getCountOfBytesReturned() {
		return CountOfBytesReturned;
	}
	public void setCountOfBytesReturned(short countOfBytesReturned) {
		CountOfBytesReturned = countOfBytesReturned;
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
	public short getCountOfBytesRead() {
		return CountOfBytesRead;
	}
	public void setCountOfBytesRead(short countOfBytesRead) {
		CountOfBytesRead = countOfBytesRead;
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
		return String.format("First Level : Read Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"CountOfByteReturn = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"bufferFormat = 0x%s , CountOfByteRead = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.CountOfBytesReturned),
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.bufferFormat),  Integer.toHexString(this.CountOfBytesRead));
	}
}
