package org.krakenapps.pcap.decoder.smb.structure;

import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;

public class SmbDirectoryInfo {

	SmbResumeKey resumeKey;
	FileAttributes fileAttributes;
	short lastWriteTime;
	short lastWriteDate;
	int fileSize;
	String filename;
	public SmbResumeKey getResumeKey() {
		return resumeKey;
	}
	public void setResumeKey(SmbResumeKey resumeKey) {
		this.resumeKey = resumeKey;
	}
	public FileAttributes getFileAttributes() {
		return fileAttributes;
	}
	public void setFileAttributes(FileAttributes fileAttributes) {
		this.fileAttributes = fileAttributes;
	}
	public short getLastWriteTime() {
		return lastWriteTime;
	}
	public void setLastWriteTime(short lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}
	public short getLastWriteDate() {
		return lastWriteDate;
	}
	public void setLastWriteDate(short lastWriteDate) {
		this.lastWriteDate = lastWriteDate;
	}
	public int getFileSize() {
		return fileSize;
	}
	public void setFileSize(int filleSize) {
		this.fileSize = filleSize;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	@Override
	public String toString(){
		return String.format("Structure : Smb Directory Info\n" +
				"resumeKey = %s\n" +
				"fileattributes = %s\n" +
				"lastWriteTime = 0x%s , lastWriteDate = 0x%s , fileSize = 0x%s\n" +
				"fileName = %s\n",
				this.resumeKey,
				this.fileAttributes,
				Integer.toHexString(this.lastWriteTime), Integer.toHexString(this.lastWriteDate), Integer.toHexString(this.fileSize),
				this.filename);
	}
}
// 43byte fixed
