package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class ReadMPXResponse implements SmbData{
	
	boolean malformed = false;
	byte WordCount;
	int offset;
	short count;
	short remaining;
	short dataCompactionMode;
	short reserved;
	short dataLength;
	short dataOffset;
	short byteCount;
	byte []pad;
	byte []data; // datalength
	public byte getWordCount() {
		return WordCount;
	}
	public void setWordCount(byte wordCount) {
		WordCount = wordCount;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public short getCount() {
		return count;
	}
	public void setCount(short count) {
		this.count = count;
	}
	public short getRemaining() {
		return remaining;
	}
	public void setRemaining(short remaining) {
		this.remaining = remaining;
	}
	public short getDataCompactionMode() {
		return dataCompactionMode;
	}
	public void setDataCompactionMode(short dataCompactionMode) {
		this.dataCompactionMode = dataCompactionMode;
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
		return String.format("First Level : Read MPX Response \n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"offset = 0x%s , count = 0x%s , remaining = 0x%s\n" +
				"dataCompactionMode = 0x%s , reserved = 0x%s , dataLength = 0x%s\n" +
				"dataOffset = 0x%s\n" +
				"byteCOunt = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.WordCount),
				Integer.toHexString(this.offset), Integer.toHexString(this.count) , Integer.toHexString(this.remaining),
				Integer.toHexString(this.dataCompactionMode) , Integer.toHexString(this.reserved) , Integer.toHexString(this.dataLength),
				Integer.toHexString(this.dataOffset),
				Integer.toHexString(this.byteCount));
	}
}
