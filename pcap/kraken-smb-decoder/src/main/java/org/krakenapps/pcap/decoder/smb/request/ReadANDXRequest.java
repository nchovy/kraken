package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class ReadANDXRequest implements SmbData{

	boolean malformed = false;
	//param
	byte wordCount;
	byte andXCommand;
	byte andXReserved;
	short andXOffset;
	short fID;
	int offset;
	short maxCountOfBytesToReturn;
	short minCountOfBytesToReturn;
	int timeout;
	short remaining;
	int offsetHigh;
	
	//data
	short byteCount;

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

	public short getFID() {
		return fID;
	}

	public void setFID(short fid) {
		fID = fid;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public short getMaxCountOfBytesToReturn() {
		return maxCountOfBytesToReturn;
	}

	public void setMaxCountOfBytesToReturn(short maxCountOfBytesToReturn) {
		this.maxCountOfBytesToReturn = maxCountOfBytesToReturn;
	}

	public short getMinCountOfBytesToReturn() {
		return minCountOfBytesToReturn;
	}

	public void setMinCountOfBytesToReturn(short minCountOfBytesToReturn) {
		this.minCountOfBytesToReturn = minCountOfBytesToReturn;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public short getRemaining() {
		return remaining;
	}

	public void setRemaining(short remaining) {
		this.remaining = remaining;
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
		return String.format("First Level : Read Andx Request\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0x%s\n" +
				"fid = 0x%s , offset = 0x%s , maxCountOfBytesToReturn = 0x%s\n" +
				"minCountofBytesToReturn = 0x%s , timeout = 0x%s , remaining = 0x%s , offsetHigh = 0x%s\n" +
				"byteCount = 0x%s(it must 0x00)\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.andXCommand), Integer.toHexString(this.andXReserved) , Integer.toHexString(this.andXOffset),
				Integer.toHexString(this.fID) , Integer.toHexString(this.offset) , Integer.toHexString(this.maxCountOfBytesToReturn) ,
				Integer.toHexString(this.minCountOfBytesToReturn) , Integer.toHexString(this.timeout) , Integer.toHexString(this.remaining) , Integer.toHexString(this.offsetHigh),
				Integer.toHexString(this.byteCount));
	}
}
