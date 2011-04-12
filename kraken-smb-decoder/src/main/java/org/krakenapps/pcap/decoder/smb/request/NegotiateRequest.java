package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbDialect;

public class NegotiateRequest implements SmbData{

	boolean malformed = false;
	//param
	byte wordCount;
	//data
	short byteCount;
	SmbDialect []dialects;
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	public SmbDialect[] getDialects() {
		return dialects;
	}
	public void setDialects(SmbDialect[] dialects) {
		this.dialects = dialects;
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
		return String.format("First Level : Negotiate Request\n"+
				"isMalformed = %s , wordCount = 0x%s , byteCount = 0x%s\n"+
				"dialect = %s\n",
				this.isMalformed() , Integer.toHexString(this.wordCount) , Integer.toHexString(this.byteCount),
				this.dialects);
	}
}
