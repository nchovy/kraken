package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class CreateTemporaryResponse implements SmbData{
	boolean malformed = false;
	byte wordCount;
	short fid;
	short byteCount;
	byte bufferFormat;
	String temporaryFileName;
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
	public String getTemporaryFileName() {
		return temporaryFileName;
	}
	public void setTemporaryFileName(String temporaryFileName) {
		this.temporaryFileName = temporaryFileName;
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
		return String.format("First Level : Create Temporary Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"fid = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"bufferFormat = 0x%s , temporaryFileName = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.fid),
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.bufferFormat) , this.temporaryFileName);
	}
}
