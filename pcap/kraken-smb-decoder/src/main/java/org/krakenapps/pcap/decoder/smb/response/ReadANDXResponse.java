package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class ReadANDXResponse implements SmbData{

	boolean malformed = false;
	byte WordCount; 
	byte andxCommand;
	byte andxReserved;
	short andxOffset;
	short available;
	short dataCompactionMode;
	short reserved1;
	short dataLength;
	short dataOffset;
	byte []reserved2 = new byte[10];
	short byteCount; // it must 0x0000
	byte pad;
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
	public byte[] getReserved2() {
		return reserved2;
	}
	public void setReserved2(byte[] reserved2) {
		this.reserved2 = reserved2;
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
		return String.format("First Level : Read Andx Response \n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0x%s\n" +
				"available = 0x%s ,  dataCompactionMode = 0x%s , reserved1 = 0x%s\n" +
				"datalength = 0x%s , dataOffset = 0x%s\n" +
				"byteCount = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.WordCount),
				Integer.toHexString(this.andxCommand) , Integer.toHexString(this.andxReserved) , Integer.toHexString(this.andxOffset),
				Integer.toHexString(this.available) , Integer.toHexString(this.dataCompactionMode) , Integer.toHexString(this.reserved1),
				Integer.toHexString(this.dataLength) , Integer.toHexString(this.dataOffset),
				Integer.toHexString(this.byteCount));
	}
}
