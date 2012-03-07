package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbResumeKey;
//0x82
public class FindRequest implements SmbData{
	private boolean malformed = false;
	//param
	private byte wordCount;
	private short maxCount;
	private FileAttributes searchAttributes;
	//data
	private short byteCount;
	private byte bufferFormat1;
	private String fileName;
	private byte bufferFormat2;
	private short resumeKeyLength;
	private SmbResumeKey []resumeKey; // new resumeKeylength mod 21
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getMaxCount() {
		return maxCount;
	}
	public void setMaxCount(short maxCount) {
		this.maxCount = maxCount;
	}
	public FileAttributes getSearchAttributes() {
		return searchAttributes;
	}
	public void setSearchAttributes(FileAttributes searchAttributes) {
		this.searchAttributes = searchAttributes;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	public byte getBufferFormat1() {
		return bufferFormat1;
	}
	public void setBufferFormat1(byte bufferFormat1) {
		this.bufferFormat1 = bufferFormat1;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public byte getBufferFormat2() {
		return bufferFormat2;
	}
	public void setBufferFormat2(byte bufferFormat2) {
		this.bufferFormat2 = bufferFormat2;
	}
	public short getResumeKeyLength() {
		return resumeKeyLength;
	}
	public void setResumeKeyLength(short resumeKeyLength) {
		this.resumeKeyLength = resumeKeyLength;
	}
	public SmbResumeKey[] getResumeKey() {
		return resumeKey;
	}
	public void setResumeKey(SmbResumeKey[] resumeKey) {
		this.resumeKey = resumeKey;
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
		return String.format("First Level : Find Request\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"maxCount = 0x%s"+
				"seachAttribute = %s\n"+
				"byteCount = 0x%s\n"+
				"bufferFormat = 0x%s , fileName = %s\n"+
				"bufferFormat2 = 0x%s , resumKeyLength = 0x%s , resumeKey = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.maxCount),
				this.searchAttributes,
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.bufferFormat1), this.fileName,
				Integer.toHexString(this.bufferFormat2) , Integer.toHexString(this.resumeKeyLength) , this.resumeKey);
	}
	
}
