package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class LockAndReadRequest implements SmbData{

	private boolean malformed = false;
	//parameter
	private byte wordCount;
	private short fid;
	private short countOfBytesToRead;
	private int readOffsetInBytes;
	private short estimateOfRemainingBytesToBeRead;
	//data
	private short byteCount;
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
	public int getReadOffsetInBytes() {
		return readOffsetInBytes;
	}
	public void setReadOffsetInBytes(int readOffsetInBytes) {
		this.readOffsetInBytes = readOffsetInBytes;
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
		return String.format("First Level : LockAndRead Request\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"fid = 0x%s , countOfByteToRead = 0x%s , readOffsetInBytes = 0x%s\n"+
				"estimateOfRemainingBytesToBeRead = 0x%s\n"+
				"byteCount = 0x%s(it must 0x00)\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.fid), Integer.toHexString(this.countOfBytesToRead) , Integer.toHexString(this.readOffsetInBytes),
				Integer.toHexString(this.estimateOfRemainingBytesToBeRead),
				Integer.toHexString(this.byteCount));
	}
}
