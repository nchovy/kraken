package org.krakenapps.pcap.decoder.smb.ntreq;

import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.structure.SrvChunk;

public class NtIoctlRequest implements TransData{

	int fucntionCode; // nt subcommand
	short fid;
	byte isFctl;
	byte isFlags;
//	FSCTL_SRV_ENUMERATE_SNAPSHOTS	0x00144064
//	Enumerate previous versions of a file.
	
//	FSCTL_SRV_REQUEST_RESUME_KEY	0x00140078
//	Retrieve an opaque file reference for server-side data movement.
	
//	FSCTL_SRV_COPYCHUNK	0x001440F2
//	Perform server-side data movement.
	byte []copyChunkResumeKey = new byte[24];
	int chunkCount;
	int reserved;
	SrvChunk []copyChunkList = new SrvChunk[chunkCount];
	//
	byte []data;
	public int getFucntionCode() {
		return fucntionCode;
	}
	public void setFucntionCode(int fucntionCode) {
		this.fucntionCode = fucntionCode;
	}
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public byte getIsFctl() {
		return isFctl;
	}
	public void setIsFctl(byte isFctl) {
		this.isFctl = isFctl;
	}
	public byte getIsFlags() {
		return isFlags;
	}
	public void setIsFlags(byte isFlags) {
		this.isFlags = isFlags;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public byte[] getCopyChunkResumeKey() {
		return copyChunkResumeKey;
	}
	public void setCopyChunkResumeKey(byte[] copyChunkResumeKey) {
		this.copyChunkResumeKey = copyChunkResumeKey;
	}
	public int getChunkCount() {
		return chunkCount;
	}
	public void setChunkCount(int chunkCount) {
		this.chunkCount = chunkCount;
	}
	public int getReserved() {
		return reserved;
	}
	public void setReserved(int reserved) {
		this.reserved = reserved;
	}
	public SrvChunk[] getCopyChunkList() {
		return copyChunkList;
	}
	public void setCopyChunkList(SrvChunk[] copyChunkList) {
		this.copyChunkList = copyChunkList;
	}
	public String toString(){
		return String.format("Second Level : Nt Ioctl Request\n"+
				"functionCode = 0x%s ,  fid = 0x%s , isFctl = 0x%s , isFlags = 0x%s , is\n",
				Integer.toHexString(this.fucntionCode), Integer.toHexString(this.fid) , Integer.toHexString(this.isFctl) , Integer.toHexString(this.isFlags));
	}
}
