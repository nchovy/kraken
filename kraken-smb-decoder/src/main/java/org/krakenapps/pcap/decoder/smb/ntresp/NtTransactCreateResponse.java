package org.krakenapps.pcap.decoder.smb.ntresp;

import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.rr.ExtFileAttributes;
import org.krakenapps.pcap.decoder.smb.rr.NamedPipeStatus;

public class NtTransactCreateResponse implements TransData{

	byte opLockLevel;
	byte reserved;
	short fid;
	int createAction;
	int eaErrorOffset;
	long creationTime;
	long lastAccessTime;
	long lastWriteTime;
	long lastChangeTime;
	ExtFileAttributes extFileAttributes;
	long allocationSize;
	long endOfFile;
	short resourceType;
	NamedPipeStatus nmPipeStatus;
	byte directory;
	public byte getOpLockLevel() {
		return opLockLevel;
	}
	public void setOpLockLevel(byte opLockLevel) {
		this.opLockLevel = opLockLevel;
	}
	public byte getReserved() {
		return reserved;
	}
	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public int getCreateAction() {
		return createAction;
	}
	public void setCreateAction(int createAction) {
		this.createAction = createAction;
	}
	public int getEaErroroffset() {
		return eaErrorOffset;
	}
	public void setEaErrorOffset(int eaErrorOffset) {
		this.eaErrorOffset = eaErrorOffset;
	}
	public long getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}
	public long getLastAccessTime() {
		return lastAccessTime;
	}
	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}
	public long getLastWriteTime() {
		return lastWriteTime;
	}
	public void setLastWriteTime(long lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}
	public long getLastChangeTime() {
		return lastChangeTime;
	}
	public void setLastChangeTime(long lastChangeTime) {
		this.lastChangeTime = lastChangeTime;
	}
	public ExtFileAttributes getExtFileAttributes() {
		return extFileAttributes;
	}
	public void setExtFileAttributes(ExtFileAttributes extFileAttributes) {
		this.extFileAttributes = extFileAttributes;
	}
	public long getAllocationSize() {
		return allocationSize;
	}
	public void setAllocationSize(long allocationSize) {
		this.allocationSize = allocationSize;
	}
	public long getEndOfFile() {
		return endOfFile;
	}
	public void setEndOfFile(long endOfFile) {
		this.endOfFile = endOfFile;
	}
	public short getResourceType() {
		return resourceType;
	}
	public void setResourceType(short resourceType) {
		this.resourceType = resourceType;
	}
	public NamedPipeStatus getNmPipeStatus() {
		return nmPipeStatus;
	}
	public void setNmPipeStatus(NamedPipeStatus nmPipeStatus) {
		this.nmPipeStatus = nmPipeStatus;
	}
	public byte getDirectory() {
		return directory;
	}
	public void setDirectory(byte directory) {
		this.directory = directory;
	}
	@Override
	public String toString(){
		return String.format("Second Level : Nt TransactCreate Response\n"+
				"opLockLevel = 0x%s , reserved = 0x%s\n , fid = 0x%s"+
				"createAction = 0x%s , eaErrorOffset = 0x%s , creationTime = 0x%s\n"+
				"lastAccessTime = 0x%s , lastWriteTime = 0x%s , lastChangeTime = 0x%s"+
				"extFileAttribute = %s , allocationSize = 0x%s , endOfFile = 0x%s\n"+
				"resourceType = 0x%s\n , nmPipeStatus = %s , directory = 0x%s",
				Integer.toHexString(this.opLockLevel) , Integer.toHexString(this.reserved) , Integer.toHexString(this.fid),
				Integer.toHexString(this.createAction) , Integer.toHexString(this.eaErrorOffset) , Long.toHexString(this.creationTime),
				Long.toHexString(this.lastAccessTime) , Long.toHexString(this.lastWriteTime) , Long.toHexString(this.lastChangeTime),
				this.extFileAttributes , Long.toHexString(this.allocationSize) , Long.toHexString(this.endOfFile),
				Integer.toHexString(this.resourceType) , this.nmPipeStatus , Integer.toHexString(this.directory));
	}
}
