package org.krakenapps.pcap.decoder.smb.structure;
import org.krakenapps.pcap.util.Buffer;

public class LockingANDXRange64{

	short pid;
	short pad;
	int byteOffsetHigh;
	int byteOffsetLow;
	int lengthInBytesHigh;
	int lengthInBytesLow;
	public short getPid() {
		return pid;
	}
	public void setPid(short pid) {
		this.pid = pid;
	}
	public short getPad() {
		return pad;
	}
	public void setPad(short pad) {
		this.pad = pad;
	}
	public int getByteOffsetHigh() {
		return byteOffsetHigh;
	}
	public void setByteOffsetHigh(int byteOffsetHigh) {
		this.byteOffsetHigh = byteOffsetHigh;
	}
	public int getByteOffsetLow() {
		return byteOffsetLow;
	}
	public void setByteOffsetLow(int byteOffsetLow) {
		this.byteOffsetLow = byteOffsetLow;
	}
	public int getLengthInBytesHigh() {
		return lengthInBytesHigh;
	}
	public void setLengthInBytesHigh(int lengthInBytesHigh) {
		this.lengthInBytesHigh = lengthInBytesHigh;
	}
	public int getLengthInBytesLow() {
		return lengthInBytesLow;
	}
	public void setLengthInBytesLow(int lengthInBytesLow) {
		this.lengthInBytesLow = lengthInBytesLow;
	}
	public void parse(Buffer b)	{
		this.setPid(b.getShort());
		this.setPad(b.getShort());
		this.setByteOffsetHigh(b.getInt());
		this.setByteOffsetLow(b.getInt());
		this.setLengthInBytesHigh(b.getInt());
		this.setLengthInBytesLow(b.getInt());
	}
	@Override
	public String toString(){
		return String.format("Structure : Locking Andx Range 64\n" +
				"pid = 0x%s , pad = 0x%s ,byteOffsetHigh = 0x%s\n" +
				"byteOffsetLow = 0x%s , lengthInBytesHigh = 0x%s, lengthInBytesLow = 0x%s\n",
				Integer.toHexString(this.pid) , Integer.toHexString(this.pad), Integer.toHexString(this.byteOffsetHigh),
				Integer.toHexString(this.byteOffsetLow) , Integer.toHexString(this.lengthInBytesHigh) , Integer.toHexString(this.lengthInBytesLow));
	}
}
