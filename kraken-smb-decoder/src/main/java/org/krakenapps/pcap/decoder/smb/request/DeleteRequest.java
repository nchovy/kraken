package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class DeleteRequest implements SmbData{
	boolean malformed = false;
	//param
	byte wordCount; // it must 0x01
	FileAttributes SearchAttributes;
	//data
	short byteCount;
	byte bufferFormat;
	String fileName;
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public FileAttributes getSearchAttributes() {
		return SearchAttributes;
	}
	public void setSearchAttributes(FileAttributes searchAttributes) {
		SearchAttributes = searchAttributes;
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
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public boolean isMalformed() {
		return malformed;
	}
	@Override
	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}
	@Override
	public String toString(){
		return String.format("First Level : Delete Request\n"+
				"isMalfored = %s\n"+
				"wordCount = 0x%s\n"+
				"searchAttribute =%s\n"+
				"byteCount = 0x%s\n"+
				"bufferFormat = 0x%s , fileName = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				this.SearchAttributes,
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.bufferFormat) , this.fileName);
	}
}
