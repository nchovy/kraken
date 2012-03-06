package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.rr.NamedPipeStatus;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class OpenANDXExtendedResponse implements SmbData{

	boolean malformed = false;
	byte wordCount; // it must 0x00
	byte andxCommand;
	byte andxReserved;
	short AndxOffset;
	short fid;
	FileAttributes fileAttrs;
	int lastWriteTime;
	int fileDataSize;
	short AccessRights;
	short ResourceType;
	NamedPipeStatus nmPipeStatus; // 2byte
	short openResults;
	int serverFid;
	int maximalAccessRight;
	int guestMaximalAccessRight;
	byte []reserved;
	//data
	short byteCount; // it must 0x0000
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
		return AndxOffset;
	}
	public void setAndxOffset(short andxOffset) {
		AndxOffset = andxOffset;
	}
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public FileAttributes getFileAttrs() {
		return fileAttrs;
	}
	public void setFileAttrs(FileAttributes fileAttrs) {
		this.fileAttrs = fileAttrs;
	}
	public int getLastWriteTime() {
		return lastWriteTime;
	}
	public void setLastWriteTime(int lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}
	public int getFileDataSize() {
		return fileDataSize;
	}
	public void setFileDataSize(int fileDataSize) {
		this.fileDataSize = fileDataSize;
	}
	public short getAccessRights() {
		return AccessRights;
	}
	public void setAccessRights(short accessRights) {
		AccessRights = accessRights;
	}
	public short getResourceType() {
		return ResourceType;
	}
	public void setResourceType(short resourceType) {
		ResourceType = resourceType;
	}
	public NamedPipeStatus getNmPipeStatus() {
		return nmPipeStatus;
	}
	public void setNmPipeStatus(NamedPipeStatus nmPipeStatus) {
		this.nmPipeStatus = nmPipeStatus;
	}
	public short getOpenResults() {
		return openResults;
	}
	public void setOpenResults(short openResults) {
		this.openResults = openResults;
	}
	public int getServerFid() {
		return serverFid;
	}
	public void setServerFid(int serverFid) {
		this.serverFid = serverFid;
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
	public byte[] getReserved() {
		return reserved;
	}
	public void setReserved(byte[] reserved2) {
		this.reserved = reserved2;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
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
		return String.format("First Level : Open Andx Extended Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0x%s\n" +
				"fid = 0x%s , fileAttrs = %s , lastWriteTime = 0x%s\n" +
				"fileDataSize = 0x%s , AccessRights = 0x%s , ResourceType = 0x%s\n" +
				"nmPipeStatus = %s\n" +
				"openResults = 0x%s , serverFid = 0x%s , maximalAccessRight = 0x%s\n" +
				"guestMaxuamlAccessRight = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.andxCommand), Integer.toHexString(this.andxReserved) , Integer.toHexString(this.AndxOffset),
				Integer.toHexString(this.fid) , this.fileAttrs , Integer.toHexString(this.lastWriteTime),
				Integer.toHexString(this.fileDataSize) , Integer.toHexString(this.AccessRights) , Integer.toHexString(this.ResourceType),
				this.nmPipeStatus,
				Integer.toHexString(this.openResults) , Integer.toHexString(this.serverFid) , Integer.toHexString(this.maximalAccessRight),
				Integer.toHexString(this.guestMaximalAccessRight));
	}
}
