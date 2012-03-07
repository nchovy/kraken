package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x80
public class QueryInfoDiskResponse implements SmbData{

	boolean malformed = false;
	byte wordCount; // it must 0x00
	short totalUnits;
	short blocksPerUnit;
	short blockSize;
	short freeUnits;
	short reserved;
	short byteCount; // it must 0x0000
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getTotalUnits() {
		return totalUnits;
	}
	public void setTotalUnits(short totalUnits) {
		this.totalUnits = totalUnits;
	}
	public short getBlocksPerUnit() {
		return blocksPerUnit;
	}
	public void setBlocksPerUnit(short blocksPerUnit) {
		this.blocksPerUnit = blocksPerUnit;
	}
	public short getBlockSize() {
		return blockSize;
	}
	public void setBlockSize(short blockSize) {
		this.blockSize = blockSize;
	}
	public short getFreeUnits() {
		return freeUnits;
	}
	public void setFreeUnits(short freeUnits) {
		this.freeUnits = freeUnits;
	}
	public short getReserved() {
		return reserved;
	}
	public void setReserved(short reserved) {
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
		return String.format("First level : Query info disk Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"totalUnits = 0x%s , blocksPerUnits = 0x%s , blockSize = 0x%s\n" +
				"freeUnit = 0x%s , resrved = 0x%s\n" +
				"byteCount = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.totalUnits), Integer.toHexString(this.blocksPerUnit) , Integer.toHexString(this.blockSize),
				Integer.toHexString(this.freeUnits)  , Integer.toHexString(this.reserved),
				Integer.toHexString(this.byteCount));
	}
}
