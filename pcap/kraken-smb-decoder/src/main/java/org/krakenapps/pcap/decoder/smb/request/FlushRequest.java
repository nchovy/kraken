package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class FlushRequest implements SmbData{

	private boolean malformed = false;
	//param	
	private byte wordCount; // it must 0x01
	private short fid;
	//data
	private short byteCount; // it must 0x0000
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
	@Override
	public String toString(){
		return String.format("First Level : Flush Request\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"fid = 0x%s\n"+
				"bytecount = 0x%s(it must 0x00)\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.fid),
				Integer.toHexString(this.byteCount));
	}
}