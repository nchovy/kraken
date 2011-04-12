package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0xC2
public class ClosePrintFileRequest implements SmbData{

	//param
	byte wordCount;
	short fid;
	//data
	short byteCount;
	boolean malformed;
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
	@Override
	public boolean isMalformed() {
		// TODO Auto-generated method stub
		return malformed;
	}
	@Override
	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}
	public String toString(){
		return String.format("First Level : Close Print File Request\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"fid = 0x%s\n"+
				"byteCount = 0x%s(it must 0x00)\n",
				this.isMalformed() ,
				Integer.toHexString(this.wordCount) ,
				Integer.toHexString(this.fid) ,
				Integer.toHexString(this.byteCount));
	}
}
