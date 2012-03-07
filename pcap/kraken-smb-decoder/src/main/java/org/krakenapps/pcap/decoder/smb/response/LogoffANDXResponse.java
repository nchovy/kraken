package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class LogoffANDXResponse implements SmbData{

	boolean malformed = false;
	byte wordCount; // it must 0x00
	byte andXCommand;
	byte andXReserved;
	short andXOffset;
	short byteCount; // it must 0x0000
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public byte getAndXCommand() {
		return andXCommand;
	}
	public void setAndXCommand(byte andXCommand) {
		this.andXCommand = andXCommand;
	}
	public byte getAndXReserved() {
		return andXReserved;
	}
	public void setAndXReserved(byte andXReserved) {
		this.andXReserved = andXReserved;
	}
	public short getAndXOffset() {
		return andXOffset;
	}
	public void setAndXOffset(short andXOffset) {
		this.andXOffset = andXOffset;
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
		return String.format("First Level : Logoff Andx Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0x%s\n" +
				"byteCount = 0x%s(it must 0x00)\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.andXCommand), Integer.toHexString(this.andXReserved), Integer.toHexString(this.andXOffset),
				Integer.toHexString(this.byteCount));
	}
}
