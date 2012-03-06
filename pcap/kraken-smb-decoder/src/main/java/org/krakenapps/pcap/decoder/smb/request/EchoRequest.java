package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x2B
public class EchoRequest implements SmbData{
	boolean malformed = false;
	//param
	byte wordCount;
	short EchoCount;
	//data
	short byteCount;
	byte []data; //new ByteCount;
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getEchoCount() {
		return EchoCount;
	}
	public void setEchoCount(short echoCount) {
		EchoCount = echoCount;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
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
		return String.format("Fisrt Level : Echo Request\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"echoCount = 0x%s\n"+
				"byteCount = 0x%s\n"+
				"data = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.EchoCount),
				Integer.toHexString(this.byteCount));
	}
}
