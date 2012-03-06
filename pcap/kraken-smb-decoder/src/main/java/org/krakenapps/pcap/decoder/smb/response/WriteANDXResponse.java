package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
// 0x2F
public class WriteANDXResponse implements SmbData{

	boolean malformed = false;
	byte wordCount; // it must 0x00
	byte andxCommand;
	byte andxReserved;
	short andxOffset;
	short count;
	short available;
	int reserved;
	short byteCount; // it must 0x0000
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
	public short getCount() {
		return count;
	}
	public void setCount(short count) {
		this.count = count;
	}
	public short getAvailable() {
		return available;
	}
	public void setAvailable(short available) {
		this.available = available;
	}
	public int getReserved() {
		return reserved;
	}
	public void setReserved(int reserved) {
		this.reserved = reserved;
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
		return String.format("First Level : Write Andx Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0x%s\n" +
				"count = 0x%s , available = 0x%s , reserved = 0x%s\n" +
				"byteCount = 0x%s(it must 0x00)\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.andxCommand) , Integer.toHexString(this.andxReserved) , Integer.toHexString(this.andxOffset),
				Integer.toHexString(this.count) , Integer.toHexString(this.available) , Integer.toHexString(this.reserved),
				Integer.toHexString(this.byteCount));
	}
}
