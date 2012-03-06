package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
// 0x08
public class QueryInfoResponse implements SmbData{

	boolean malformed = false;
	byte WordCount;
	FileAttributes fileAttributes;
	int lastWriteTime;
	int fileSize;
	byte[] reserved = new byte[10]; // it all 0x00
	short byteCount;
	public byte getWordCount() {
		return WordCount;
	}
	public void setWordCount(byte wordCount) {
		WordCount = wordCount;
	}
	public FileAttributes getFileAttributes() {
		return fileAttributes;
	}
	public void setFileAttributes(FileAttributes fileAttributes) {
		this.fileAttributes = fileAttributes;
	}
	public int getLastWriteTime() {
		return lastWriteTime;
	}
	public void setLastWriteTime(int lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	public byte[] getReserved() {
		return reserved;
	}
	public void setReserved(byte[] reserved) {
		this.reserved = reserved;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
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
		return String.format("First Level : Query Info Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"fileAttribute = %s , lastWriteTime = 0x%s , fileSize = 0x%s\n" +
				"byteCount = 0x%s(it must 0x00)\n",
				this.malformed,
				this.WordCount,
				this.fileAttributes , this.lastWriteTime , this.fileSize,
				this.byteCount);
	}
}
