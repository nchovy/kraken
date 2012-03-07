package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x2C
public class WriteAndCloseRequest implements SmbData{

	boolean malformed = false;
	//param
	byte wordCount;
	short fid;
	short countOfBytesToWrite;
	int writeOffsetInBytes;
	int lastWriteTime;
	byte []reserved = new byte[12]; // 12byte
	//data
	short byteCount;
	byte pad;
	byte []data; // new ByteCount;
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
	public short getCountOfBytesToWrite() {
		return countOfBytesToWrite;
	}
	public void setCountOfBytesToWrite(short countOfBytesToWrite) {
		this.countOfBytesToWrite = countOfBytesToWrite;
	}
	public int getWriteOffsetInBytes() {
		return writeOffsetInBytes;
	}
	public void setWriteOffsetInBytes(int writeOffsetInBytes) {
		this.writeOffsetInBytes = writeOffsetInBytes;
	}
	public int getLastWriteTime() {
		return lastWriteTime;
	}
	public void setLastWriteTime(int lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
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
	public byte getPad() {
		return pad;
	}
	public void setPad(byte pad) {
		this.pad = pad;
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
		return String.format("First Level : WriteAndClose Request\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"fid = 0x%s , countOfBytesToWrite = 0x%s , writeoffsetInBytes = 0x%s\n" +
				"lastWriteTime = 0x%s , reserved = %s\n" +
				"byteCount = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.fid) , Integer.toHexString(this.countOfBytesToWrite) , Integer.toHexString(this.writeOffsetInBytes),
				Integer.toHexString(this.lastWriteTime) , this.reserved.toString(),
				Integer.toHexString(this.byteCount));
	}
}
