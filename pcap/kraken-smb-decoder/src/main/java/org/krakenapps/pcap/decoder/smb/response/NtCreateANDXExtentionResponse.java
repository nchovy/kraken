package org.krakenapps.pcap.decoder.smb.response;

import org.krakenapps.pcap.decoder.smb.rr.ExtFileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class NtCreateANDXExtentionResponse implements SmbData{

	boolean malformed = false;
	//oplocklevel
	public final static byte NO_OP_GRANTED = 0x00;
	public final static byte EXCLUSIVE_OPLOCK_GRANTED = 0x01;
	public final static byte BATCH_OPLOCK_GRANTED = 0x02;
	public final static byte LEVEL_2_OPLOCK_GRANTED = 0x03;
	
	//Resourcetype;
	public final static short FILE_TYPE_DISK = 0x0000;
	public final static short FILE_TYPE_BYTE_MODE_PIPE = 0x0001;
	public final static short FILE_TYPE_MESSAGE_MODE_PIPE = 0x0002;
	public final static short FILE_TYPE_PRINTER = 0x0003;
	public final static short FILE_TYPE_UNKNOWN = (short) 0xFFFF;
	
	//FileStatusFlags;
	public final static short NO_EAS = 0x0001;
	public final static short NO_SUBSTREAMS = 0x00002;
	public final static short NO_REPARSETAG = 0x0004;
	
	byte wordCount; // it must 0x00
	byte andxCommand;
	byte andxReserved;
	short andxOffset;
	byte opLockLevel;
	short fid;
	int creationAction;
	int createTime;
	int lastAccessTime;
	int lastWriteTime;
	int lastChangeTime;
	ExtFileAttributes extFileAttributes;
	long allocationSize;
	long endOfFile;
	short ResourceType;
	short nmPipeStatus_or_FileStatusFlag;
	boolean isNmPipeStatus;
	
	byte []volumeGUID = new byte[16];
	long fileID;
	int maximalAccessRight;
	int guestMaximalAccessRight;
	
	byte directory;
	short byteCount; // it must 0x0000
	
	
	public boolean isNmPipeStatus() {
		return isNmPipeStatus;
	}
	private void setNmPipeStatus(boolean isNmPipeStatus) {
		this.isNmPipeStatus = isNmPipeStatus;
	}
	public int getCreationAction() {
		return creationAction;
	}
	public void setCreationAction(int creationAction) {
		this.creationAction = creationAction;
	}
	public short getNmPipeStatus_or_FileStatusFlag() {
		return nmPipeStatus_or_FileStatusFlag;
	}
	public void setNmPipeStatus_or_FileStatusFlag(
			short nmPipeStatus_or_FileStatusFlag) {
		this.nmPipeStatus_or_FileStatusFlag = nmPipeStatus_or_FileStatusFlag;
		if(this.isResourceFileTypeDisk() == true)
			this.setNmPipeStatus(true);
		else
			this.setNmPipeStatus(false);
	}
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public byte getAndxCommand() {
		return andxCommand;
	}
	public void setAndxCommand(byte andxCommand) {
		this.andxCommand = andxCommand;
	}
	public byte getAndxReserved() {
		return andxReserved;
	}
	public void setAndxReserved(byte andxReserved) {
		this.andxReserved = andxReserved;
	}
	public short getAndxOffset() {
		return andxOffset;
	}
	public void setAndxOffset(short andxOffset) {
		this.andxOffset = andxOffset;
	}
	public byte getOpLockLevel() {
		return opLockLevel;
	}
	public void setOpLockLevel(byte opLockLevel) {
		this.opLockLevel = opLockLevel;
	}
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public int getCreateTime() {
		return createTime;
	}
	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}
	public int getLastAccessTime() {
		return lastAccessTime;
	}
	public void setLastAccessTime(int lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}
	public int getLastWriteTime() {
		return lastWriteTime;
	}
	public void setLastWriteTime(int lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}
	public int getLastChangeTime() {
		return lastChangeTime;
	}
	public void setLastChangeTime(int lastChangeTime) {
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
		return ResourceType;
	}
	public void setResourceType(short resourceType) {
		ResourceType = resourceType;
	}
	public byte[] getVolumeGUID() {
		return volumeGUID;
	}
	public void setVolumeGUID(byte[] volumeGUID) {
		this.volumeGUID = volumeGUID;
	}
	public long getFileID() {
		return fileID;
	}
	public void setFileID(long fileID) {
		this.fileID = fileID;
	}
	public int getMaximalAccessRight() {
		return maximalAccessRight;
	}
	public void setMaximalAccessRight(int maximalAccessRight) {
		this.maximalAccessRight = maximalAccessRight;
	}
	public int getGuestMaximalAccessRight() {
		return guestMaximalAccessRight;
	}
	public void setGuestMaximalAccessRight(int guestMaximalAccessRight) {
		this.guestMaximalAccessRight = guestMaximalAccessRight;
	}
	public byte getDirectory() {
		return directory;
	}
	public void setDirectory(byte directory) {
		this.directory = directory;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	
	public boolean isResourceFileTypeDisk()
	{
		if((this.ResourceType & FILE_TYPE_DISK) == FILE_TYPE_DISK)
			return true;
		else
			return false;
	}
	public boolean isResourceFileTypeByteModePipe()
	{
		if((this.ResourceType & FILE_TYPE_BYTE_MODE_PIPE) == FILE_TYPE_BYTE_MODE_PIPE)
			return true;
		else
			return false;
	}
	public boolean isResourceFileTypeMessageModePipe()
	{
		if((this.ResourceType & FILE_TYPE_MESSAGE_MODE_PIPE) ==FILE_TYPE_MESSAGE_MODE_PIPE)
			return true;
		else
			return false;
	}
	public boolean isResourceFileTypePrinter()
	{
		if((this.ResourceType & FILE_TYPE_PRINTER) == FILE_TYPE_PRINTER)
			return true;
		else
			return false;	
	}
	public boolean isResourceFileTypeUnkown()
	{
		if((this.ResourceType & FILE_TYPE_UNKNOWN) == FILE_TYPE_UNKNOWN)
			return true;
		else
			return false;
	}
	//FileStatusFlags;
	public boolean isFileStatusNoEas()
	{
		if((this.nmPipeStatus_or_FileStatusFlag & NO_EAS) == NO_EAS)
			return true;
		else
			return false;
	}
	public boolean isFileStatusNoSubstream()
	{
		if((this.nmPipeStatus_or_FileStatusFlag & NO_SUBSTREAMS) == NO_SUBSTREAMS)
			return true;
		else
			return false;
	}
	public boolean isFileStatusNoReparsetag()
	{
		if((this.nmPipeStatus_or_FileStatusFlag & NO_REPARSETAG) == NO_REPARSETAG)
			return true;
		else
			return false;
	}
	public boolean isOplockNoOpGranted()
	{
		if((this.opLockLevel & NO_OP_GRANTED) == NO_OP_GRANTED)
			return true;
		else
			return false;
	}
	public boolean isOplockExclusiveOplockGranted()
	{
		if((this.opLockLevel & EXCLUSIVE_OPLOCK_GRANTED) ==EXCLUSIVE_OPLOCK_GRANTED)
			return true;
		else
			return false;
	}
	public boolean isOplockBatchOplockGranted()
	{
		if((this.opLockLevel & BATCH_OPLOCK_GRANTED) == BATCH_OPLOCK_GRANTED)
			return true;
		else
			return false;
	}
	public boolean isOplockLevel2OplockGranted()
	{
		if((this.opLockLevel & LEVEL_2_OPLOCK_GRANTED) == LEVEL_2_OPLOCK_GRANTED)
			return true;
		else
			return false;
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
		return String.format("First Level : Nt Create Andx Extention Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0x%s\n" +
				"opLockLevel = 0x%s , fid = 0x%s , creationAction = 0x%s\n" +
				"createTime = 0x%s , lastAccessTime = 0x%s , lastWriteTime = 0x%s\n" +
				"lastChangeTime = 0x%s , extFileAttributes = %s , allcationSize = 0x%s\n" +
				"endOfFile = 0x%s , resourceType = 0x%s\n" +
				"nmPipeStatus_or_FileStatusFlag = 0x%s\n" +
				"isNmPipeStatus(true is nmPipeStatus) = %s\n" +
				"volumeGUID = %s\n" +
				"fileID = 0x%s , maximalAccessRight = 0x%s, guestMaximalAccessRight = 0x%s , directory = 0x%s\n" +
				"byteCount = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.andxCommand), Integer.toHexString(this.andxReserved) , Integer.toHexString(this.andxOffset),
				Integer.toHexString(this.opLockLevel) , Integer.toHexString(this.fid) , Integer.toHexString(this.creationAction),
				Integer.toHexString(this.createTime) , Integer.toHexString(this.lastAccessTime) , Integer.toHexString(this.lastWriteTime),
				Integer.toHexString(this.lastChangeTime) ,this.extFileAttributes , Long.toHexString(this.allocationSize),
				Long.toHexString(this.endOfFile) , Integer.toHexString(this.ResourceType),
				Integer.toHexString(this.nmPipeStatus_or_FileStatusFlag),
				this.isNmPipeStatus,
				this.volumeGUID.toString(),
				Long.toHexString(this.fileID) , Integer.toHexString(this.maximalAccessRight) , Integer.toHexString(this.guestMaximalAccessRight) , Integer.toHexString(this.directory),
				Integer.toHexString(this.byteCount));
	}
}
