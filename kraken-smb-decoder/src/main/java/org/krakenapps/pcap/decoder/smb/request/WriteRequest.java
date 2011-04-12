package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
// 0x0B
public class WriteRequest implements SmbData{

	boolean malformed = false;
	//param
	short wordCount;
	short fid;
	short countOfBytesToWrtie;
	int  writeoffsetInBytes;
	short estimateofRemainingBytesToBeWritten;
	
	//data
	short byteCount;
	byte bufferFormat;
	short datalength;
	byte []data; // new [CountofBytestoWirte] 
	public short getWordCount() {
		return wordCount;
	}
	public void setWordCount(short wordCount) {
		this.wordCount = wordCount;
	}
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public short getCountOfBytesToWrtie() {
		return countOfBytesToWrtie;
	}
	public void setCountOfBytesToWrtie(short countOfBytesToWrtie) {
		this.countOfBytesToWrtie = countOfBytesToWrtie;
	}
	public int getWriteoffsetInBytes() {
		return writeoffsetInBytes;
	}
	public void setWriteoffsetInBytes(int writeoffsetInBytes) {
		this.writeoffsetInBytes = writeoffsetInBytes;
	}
	public short getEstimateofRemainingBytesToBeWritten() {
		return estimateofRemainingBytesToBeWritten;
	}
	public void setEstimateofRemainingBytesToBeWritten(
			short estimateofRemainingBytesToBeWritten) {
		this.estimateofRemainingBytesToBeWritten = estimateofRemainingBytesToBeWritten;
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
	public short getDatalength() {
		return datalength;
	}
	public void setDatalength(short datalength) {
		this.datalength = datalength;
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
		return String.format("First Level : Write Request\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"fid = 0x%s , countOfBytesToWrite = 0x%s ,  writeoffsetInBytes = 0x%s\n" +
				"estimateofRemainingbytestobeWritten = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"bufferFormat = 0x%s , dataLength = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.fid), Integer.toHexString(this.countOfBytesToWrtie) , Integer.toHexString(this.writeoffsetInBytes),
				Integer.toHexString(this.estimateofRemainingBytesToBeWritten),
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.bufferFormat) , Integer.toHexString(this.datalength));
	}
}
