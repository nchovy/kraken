package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x14
public class WriteAndUnlockRequest implements SmbData{

	boolean malformed = false;
	//parameter
	byte wordCount;
	short fid;
	short countOfBytesToWrite;
	short writeOffsetInBytes;
	short estimateOfRemainingBytesToBeWritten;
	//data
	short byteCount;
	byte bufferFormat;
	short dataLength;
	byte []data;// new DataLength
	
	public short getWriteOffsetInBytes() {
		return writeOffsetInBytes;
	}
	public void setWriteOffsetInBytes(short writeOffsetInBytes) {
		this.writeOffsetInBytes = writeOffsetInBytes;
	}
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
	public short getCountOfBytesToWrite() {
		return countOfBytesToWrite;
	}
	public void setCountOfBytesToWrite(short countOfBytesToWrite) {
		this.countOfBytesToWrite = countOfBytesToWrite;
	}
	public short getEstimateOfRemainingBytesToBeWritten() {
		return estimateOfRemainingBytesToBeWritten;
	}
	public void setEstimateOfRemainingBytesToBeWritten(
			short estimateOfRemainingBytesToBeWritten) {
		this.estimateOfRemainingBytesToBeWritten = estimateOfRemainingBytesToBeWritten;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	public byte getBufferFormat() {
		return bufferFormat;
	}
	public void setBufferFormat(byte bufferFormat) {
		this.bufferFormat = bufferFormat;
	}
	public short getDataLength() {
		return dataLength;
	}
	public void setDataLength(short dataLength) {
		this.dataLength = dataLength;
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
		return String.format("First Level : Write And Unlcok Request\n" +
				"isMalfored = %s\n" +
				"wordCount = 0x%s\n" +
				"fid = 0x%s , countOfBytesToWrite = 0x%s , writeOffsetInBytes = 0x%s, estimateOfRemaningBytesToBeWritten = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"bufferFormat = 0x%s , dataLength = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.fid) , Integer.toHexString(this.countOfBytesToWrite) , Integer.toHexString(this.writeOffsetInBytes) , Integer.toHexString(this.estimateOfRemainingBytesToBeWritten),
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.bufferFormat), Integer.toHexString(this.dataLength));
	}
}
