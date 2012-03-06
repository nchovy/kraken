package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x24
public class LockingANDXResponse implements SmbData{

	boolean malformed = false;
	byte wordCount;
	byte andxCommand; // 0xFF no additional SMB command 
	byte andxReserved; // 0x00
	byte andxOffset;
	short byteCount;
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
	public byte getAndxOffset() {
		return andxOffset;
	}
	public void setAndxOffset(byte andxOffset) {
		this.andxOffset = andxOffset;
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
		return String.format("First Level : Locking Andx Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"andxCommand = 0x%s, andxReserved = 0x%s , andxOffset = 0x%s\n" +
				"byteCount = 0x%s(it must 0x00)\n" ,
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.andxCommand) , Integer.toHexString(this.andxReserved) , Integer.toHexString(this.andxOffset),
				Integer.toHexString(this.byteCount));
	}
}
