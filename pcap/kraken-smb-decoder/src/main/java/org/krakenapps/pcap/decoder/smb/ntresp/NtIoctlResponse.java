package org.krakenapps.pcap.decoder.smb.ntresp;

import org.krakenapps.pcap.decoder.smb.TransData;

public class NtIoctlResponse implements TransData{
	short functionCode;
	byte []data;
//	FSCTL_SRV_ENUMERATE_SNAPSHOTS	0x00144064
//	Enumerate previous versions of a file.
	int numberOfSnapShots;
	int numberOfSnapShotsReturned;
	int snapShotArraySize;
	byte []snapShotMultiSZ;
//	FSCTL_SRV_REQUEST_RESUME_KEY	0x00140078
//	Retrieve an opaque file reference for server-side data movement.
	byte []copychunkResumeKey = new byte[24];
	int contextLength;
	byte []context = new byte[contextLength];
//	FSCTL_SRV_COPYCHUNK	0x001440F2
//	Perform server-side data movement.
	int chunksWritten;
	int chunkBytesWritten;
	int totalBytesWritten;
	
	//
	public short getFunctionCode() {
		return functionCode;
	}
	public void setFunctionCode(short functionCode) {
		this.functionCode = functionCode;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public int getNumberOfSnapShots() {
		return numberOfSnapShots;
	}
	public void setNumberOfSnapShots(int numberOfSnapShots) {
		this.numberOfSnapShots = numberOfSnapShots;
	}
	public int getNumberOfSnapShotsReturned() {
		return numberOfSnapShotsReturned;
	}
	public void setNumberOfSnapShotsReturned(int numberOfSnapShotsReturned) {
		this.numberOfSnapShotsReturned = numberOfSnapShotsReturned;
	}
	public int getSnapShotArraySize() {
		return snapShotArraySize;
	}
	public void setSnapShotArraySize(int snapShotArraySize) {
		this.snapShotArraySize = snapShotArraySize;
	}
	public byte[] getSnapShotMultiSZ() {
		return snapShotMultiSZ;
	}
	public void setSnapShotMultiSZ(byte[] snapShotMultiSZ) {
		this.snapShotMultiSZ = snapShotMultiSZ;
	}
	public byte[] getCopychunkResumeKey() {
		return copychunkResumeKey;
	}
	public void setCopychunkResumeKey(byte[] copychunkResumeKey) {
		this.copychunkResumeKey = copychunkResumeKey;
	}
	public int getContextLength() {
		return contextLength;
	}
	public void setContextLength(int contextLength) {
		this.contextLength = contextLength;
	}
	public byte[] getContext() {
		return context;
	}
	public void setContext(byte[] context) {
		this.context = context;
	}
	public int getChunksWritten() {
		return chunksWritten;
	}
	public void setChunksWritten(int chunksWritten) {
		this.chunksWritten = chunksWritten;
	}
	public int getChunkBytesWritten() {
		return chunkBytesWritten;
	}
	public void setChunkBytesWritten(int chunkBytesWritten) {
		this.chunkBytesWritten = chunkBytesWritten;
	}
	public int getTotalBytesWritten() {
		return totalBytesWritten;
	}
	public void setTotalBytesWritten(int totalBytesWritten) {
		this.totalBytesWritten = totalBytesWritten;
	}
	@Override
	public String toString(){
		return String.format("Second Level : Nt Ioctl Response\n"+
				"functionCode = 0x%s , data = %s\n"+
				"numberOfSnapShots = 0x%s , numberOfSnapshotsReturned = 0x%s , snapShotArraySize = 0x%s\n"+
				"snapShotMultiSZ = %s , copyShunkResumeKey = %s , contextLength = 0x%s"+
				"context = %s , chunksWritten = 0x%s , chunkBytesWirtten = 0x%s\n"+
				"totalBytesWritten = 0x%s\n",
				Integer.toHexString(this.functionCode) , this.data.toString(),
				Integer.toHexString(this.numberOfSnapShots) , Integer.toHexString(this.numberOfSnapShotsReturned) , Integer.toHexString(this.snapShotArraySize),
				this.snapShotMultiSZ.toString() , this.copychunkResumeKey.toString() , Integer.toHexString(this.contextLength),
				this.context.toString() , Integer.toHexString(this.chunksWritten) , Integer.toHexString(this.chunkBytesWritten),
				Integer.toHexString(this.totalBytesWritten));
	}
}
