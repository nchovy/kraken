package org.krakenapps.pcap.decoder.smb.response;

import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class ReadANDXExtensionResponse implements SmbData{

	boolean malformed = false;
	byte WordCount; 
	byte andxCommand;
	byte andxReserved;
	short andxOffset;
	short available;
	short dataCompactionMode;
	short reserved1;
	int dataLength;
	short dataOffset;
	int dataLengthHigh;
	byte []reserved2 = new byte[8];
	int byteCount; 
	byte []pad;
	byte []data; // DataLength;
	public byte getWordCount() {
		return WordCount;
	}
	public void setWordCount(byte wordCount) {
		WordCount = wordCount;
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
	public short getAvailable() {
		return available;
	}
	public void setAvailable(short available) {
		this.available = available;
	}
	public short getDataCompactionMode() {
		return dataCompactionMode;
	}
	public void setDataCompactionMode(short dataCompactionMode) {
		this.dataCompactionMode = dataCompactionMode;
	}
	public short getReserved1() {
		return reserved1;
	}
	public void setReserved1(short reserved1) {
		this.reserved1 = reserved1;
	}
	public int getDataLength() {
		return dataLength & 0xffffffff;
	}
	public void setDataLength(int dataLength) {
		if(dataLength <0){
			dataLength = dataLength & 0x0000ffff;
		}
		this.dataLength = dataLength;
	}
	public short getDataOffset() {
		return dataOffset;
	}
	public void setDataOffset(short dataOffset) {
		this.dataOffset = dataOffset;
	}
	public int getDataLengthHigh() {
		return dataLengthHigh;
	}
	public void setDataLengthHigh(int dataLengthHigh) {
		this.dataLengthHigh = dataLengthHigh;
	}
	public byte[] getReserved2() {
		return reserved2;
	}
	public void setReserved2(byte[] reserved2) {
		this.reserved2 = reserved2;
	}
	public int getByteCount() {
		return byteCount & 0xffffffff;
	}
	public void setByteCount(int byteCount) {
		if(byteCount <0){
			byteCount = byteCount & 0x0000ffff;
		}
		this.byteCount = byteCount;
	}
	public byte[] getPad() {
		return pad;
	}
	public void setPad(byte[] pad2) {
		this.pad = pad2;
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
		return String.format("First Level : Read Andx Extension Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0x%s\n" +
				"available = 0x%s , dataCompactionMode = 0x%s , reserved1 = 0x%s\n" +
				"dataLength = 0x%s , dataOffset = 0x%s , DataLengthHigh = 0x%s\n" +
				"byteCount = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.WordCount),
				Integer.toHexString(this.andxCommand) , Integer.toHexString(this.andxReserved) , Integer.toHexString(this.andxOffset),
				Integer.toHexString(this.available) , Integer.toHexString(this.dataCompactionMode) , Integer.toHexString(this.reserved1),
				Integer.toHexString(this.dataLength) , Integer.toHexString(this.dataOffset) , Integer.toHexString(this.dataLengthHigh),
				Integer.toHexString(this.byteCount));
	}
}
