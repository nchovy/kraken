package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class OpenRequest implements SmbData{
	boolean malformed = false;
	//parameter
	byte wordCount;
	short accessMode;
	FileAttributes searchAttributes;
	//data
	short byteCount;
	byte []bytes;
	byte bufferFormat;
	String fileName;
	public byte getwordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getAccessMode() {
		return accessMode;
	}
	public void setAccessMode(short accessMode) {
		this.accessMode = accessMode;
	}
	public FileAttributes getSearchAttributes() {
		return searchAttributes;
	}
	public void setSearchAttributes(FileAttributes searchAttribytes) {
		this.searchAttributes = searchAttribytes;
	}
	public short getbyteCount() {
		return byteCount;
	}
	public void setbyteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	public byte getWordCount() {
		return wordCount;
	}
	public byte[] getBytes() {
		return bytes;
	}
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	public byte getBufferFormat() {
		return bufferFormat;
	}
	public void setBufferFormat(byte bufferFormat) {
		this.bufferFormat = bufferFormat;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	// TODO AccessMode parsing
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
		return String.format("First Level : Open Request\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"accessMode = 0x%s ,searchAttribute = %s\n" +
				"byteCount = 0x%s\n" +
				"bufferFormat = 0x%s , fileName = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				this.accessMode,this.searchAttributes,
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.bufferFormat) , this.fileName);
	}
}
