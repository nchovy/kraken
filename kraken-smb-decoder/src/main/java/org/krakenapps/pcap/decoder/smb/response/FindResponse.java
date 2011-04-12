package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbDirectoryInfo;
//0x82
public class FindResponse implements SmbData{
	boolean malformed = false;
	byte wordCount; // it must 0x00
	short count;
	short byteCount; // it must 0x0000
	byte bufferFormat;
	short dataLength;
	SmbDirectoryInfo []directoryInformationData; // new DataLength mod 43
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getCount() {
		return count;
	}
	public void setCount(short count) {
		this.count = count;
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
	public SmbDirectoryInfo[] getDirectoryInformationData() {
		return directoryInformationData;
	}
	public void setDirectoryInformationData(
			SmbDirectoryInfo[] directoryInformationData) {
		this.directoryInformationData = directoryInformationData;
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
		return String.format("First Level : Find Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"count = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"bufferFormat = 0x%s , dataLength = 0x%s\n" +
				"directoryInfoData = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.count),
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.bufferFormat), Integer.toHexString(this.dataLength),
				this.directoryInformationData);
	}
}
