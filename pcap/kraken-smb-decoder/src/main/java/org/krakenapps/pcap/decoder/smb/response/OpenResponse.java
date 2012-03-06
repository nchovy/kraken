package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class OpenResponse implements SmbData{

	boolean malformed = false;
	byte wordCount;
	short fid;
	FileAttributes fileAttrs;
	int fileSize;
	int lastModified;
	int accessMode;
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
	public FileAttributes getFileAttrs() {
		return fileAttrs;
	}
	public void setFileAttrs(FileAttributes fileAttrs) {
		this.fileAttrs = fileAttrs;
	}
	public int getFileSize() {
		return fileSize;
	}
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	public int getLastModified() {
		return lastModified;
	}
	public void setLastModified(int lastModified) {
		this.lastModified = lastModified;
	}
	public int getAccessMode() {
		return accessMode;
	}
	public void setAccessMode(int accessMode) {
		this.accessMode = accessMode;
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
		return String.format("First Level : Open Response\n" +
				"isMalformed = %s\n" +
				"fid = 0x%s , fileAttrs = %s , fileSize = 0x%s\n" +
				"lastModify = 0x%s ,  , accessMode = 0x%s\n",
				this.malformed,
				this.fid, this.fileAttrs , this.fileSize,
				this.lastModified , this.accessMode);
	}
}