package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;



public class ReadRequest implements SmbData{
//	ByteBuffer buffer;
	//param
	boolean malformed = false;
	byte wordCount;
	short fid;
	short countOfBytesToRead;
	int readOffSetInBytes;
	short estimateOfRemainingBytesToBeRead;
	//data
	short byteCount;
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public short getCountOfBytesToRead() {
		return countOfBytesToRead;
	}
	public void setCountOfBytesToRead(short countOfBytesToRead) {
		this.countOfBytesToRead = countOfBytesToRead;
	}
	public int getReadOffSetInBytes() {
		return readOffSetInBytes;
	}
	public void setReadOffSetInBytes(int readOffSetInBytes) {
		this.readOffSetInBytes = readOffSetInBytes;
	}
	public short getEstimateOfRemainingBytesToBeRead() {
		return estimateOfRemainingBytesToBeRead;
	}
	public void setEstimateOfRemainingBytesToBeRead(
			short estimateOfRemainingBytesToBeRead) {
		this.estimateOfRemainingBytesToBeRead = estimateOfRemainingBytesToBeRead;
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
		return String.format("First Level : Read Request\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"fid = 0x%s, countOfBytesToRead = 0x%s , readoffsetInBytes = 0x%s , estimateOfRemainingBytesToRead = 0x%s\n" +
				"byteCount = 0x%s(it must 0x00)\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.fid) , Integer.toHexString(this.countOfBytesToRead) , Integer.toHexString(this.readOffSetInBytes) , Integer.toHexString(this.estimateOfRemainingBytesToBeRead),
				Integer.toHexString(this.byteCount));
	}
}
