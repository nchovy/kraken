package org.krakenapps.pcap.decoder.smb.structure;

public class SrvChunk {

	long SourceOffset;
	long DestinationOffset;
	int CopyLength;
	int Reserved;
	public long getSourceOffset() {
		return SourceOffset;
	}
	public void setSourceOffset(long sourceOffset) {
		SourceOffset = sourceOffset;
	}
	public long getDestinationOffset() {
		return DestinationOffset;
	}
	public void setDestinationOffset(long destinationOffset) {
		DestinationOffset = destinationOffset;
	}
	public int getCopyLength() {
		return CopyLength;
	}
	public void setCopyLength(int copyLength) {
		CopyLength = copyLength;
	}
	public int getReserved() {
		return Reserved;
	}
	public void setReserved(int reserved) {
		Reserved = reserved;
	}
	@Override
	public String toString(){
		return String.format("Structure : SrvSChunk\n" +
				"SourceOffset = 0x%s, DestinationOffset = 0x%s\n" +
				"CopyLength = 0x%s , Reserved = 0x%s\n",
				Long.toHexString(this.SourceOffset) , Long.toHexString(this.DestinationOffset),
				Integer.toHexString(this.CopyLength) , Integer.toHexString(this.Reserved));
	}
}
