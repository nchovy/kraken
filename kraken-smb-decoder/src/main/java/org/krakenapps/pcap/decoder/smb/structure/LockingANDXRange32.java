package org.krakenapps.pcap.decoder.smb.structure;
import org.krakenapps.pcap.util.Buffer;

public class LockingANDXRange32{

	short pid;
	int byteOffset;
	int lengthInBytes;
	public short getPid() {
		return pid;
	}
	public void setPid(short pid) {
		this.pid = pid;
	}
	public int getByteOffset() {
		return byteOffset;
	}
	public void setByteOffset(int byteOffset) {
		this.byteOffset = byteOffset;
	}
	public int getLengthInBytes() {
		return lengthInBytes;
	}
	public void setLengthInBytes(int lengthInBytes) {
		this.lengthInBytes = lengthInBytes;
	}
	public void parse(Buffer b)
	{
		this.setPid(b.getShort());
		this.setByteOffset(b.getInt());
		this.setLengthInBytes(b.getInt());
	}
	@Override
	public String toString(){
		return String.format("Structure : Locking Andx Range32 \n" +
				"pid = 0x%s , byteOffset = 0x%s , lengthInBytes = 0x%s\n",
				Integer.toHexString(this.pid) , Integer.toHexString(this.byteOffset), Integer.toHexString(this.lengthInBytes));
	}

}
