package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class SetInfoResponse implements SmbData{
	
	boolean malformed = false;
	byte WordCount;
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
		return String.format("First Level : Set Info Response\n" +
				"isMalformed =  %s\n" +
				"wordCount = 0x%s(it must 0x00)\n" +
				"byteCount = 0x%s(it must 0x00)\n",
				this.malformed,
				Integer.toHexString(this.WordCount),
				Integer.toHexString(this.byteCount));
	}
}
