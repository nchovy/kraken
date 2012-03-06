package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x80
public class QueryInfoDiskRequest implements SmbData{

	boolean malformed = false;
	//param
	byte WordCount;
	//data
	short byteCount;
	public byte getWordCount() {
		return WordCount;
	}
	public void setWordCount(byte wordCount) {
		WordCount = wordCount;
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
		return String.format("First Level : Query Info Disk Request\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s(it must 0x00)\n" +
				"bytecount = 0x%s(it must 0x00)\n",
				this.malformed,
				Integer.toHexString(this.WordCount),
				Integer.toHexString(this.byteCount));
	}
	
}
