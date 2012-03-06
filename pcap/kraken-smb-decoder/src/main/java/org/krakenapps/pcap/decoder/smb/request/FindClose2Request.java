package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x34
public class FindClose2Request implements SmbData{

	boolean malformed = false;
	//param
	byte wordCount;
	short searchHandle;
	//data
	short byteCount;
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getSearchHandle() {
		return searchHandle;
	}
	public void setSearchHandle(short searchHandle) {
		this.searchHandle = searchHandle;
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
		return String.format("First Level : Find Close2 Request\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"searchHandle = 0x%s\n"+
				"byteCount = 0x%s(it msut 0x00)\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.searchHandle),
				Integer.toHexString(this.byteCount));
	}
}
