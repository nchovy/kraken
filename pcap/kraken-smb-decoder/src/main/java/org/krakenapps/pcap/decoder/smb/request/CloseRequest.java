package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class CloseRequest implements SmbData{

	//param
	short wordCount;
	short fid;
	int lastTimeModified;
	boolean malformed;
	//data
	short byteCount;
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
	public int getLastTimeModified() {
		return lastTimeModified;
	}
	public void setLastTimeModified(int lastTimeModified) {
		this.lastTimeModified = lastTimeModified;
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
		return String.format("First Level : Close Request\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"fid = 0x%s , lastModified = 0x%s\n"+
				"byteCount = 0x%s(it must 0x00)\n",
				this.isMalformed() ,
				Integer.toHexString(this.wordCount) ,
				Integer.toHexString(this.fid), Integer.toHexString(this.lastTimeModified) ,
				Integer.toHexString(this.byteCount));
	}

}
