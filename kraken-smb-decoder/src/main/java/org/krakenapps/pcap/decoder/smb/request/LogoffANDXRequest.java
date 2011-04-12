package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class LogoffANDXRequest implements SmbData{

	boolean malformed = false;
	//param
	byte wordCount;
	byte andxCommand;
	byte andxResrved;
	
	short andxOffset;
	//data
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
	public byte getAndxResrved() {
		return andxResrved;
	}
	public void setAndxResrved(byte andxResrved) {
		this.andxResrved = andxResrved;
	}
	public short getAndxOffset() {
		return andxOffset;
	}
	public void setAndxOffset(short andxOffset) {
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
		return String.format("First Level : LogOff Andx Request\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"andxCommand =0x%s , andxReserved = 0x%s, andxOffset = 0x%s\n"+
				"byteCount = 0x%s(it must 0x00)\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.andxCommand), Integer.toHexString(this.andxResrved) , Integer.toHexString(this.andxOffset),
				Integer.toHexString(this.byteCount));
	}
}
