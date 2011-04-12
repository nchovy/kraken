package org.krakenapps.pcap.decoder.smb.trans2resp;

import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;

public class Open2Response implements TransData{
	
	short fid;
	FileAttributes fileAttribute;
	int creationTime;
	int fileDataSize;
	short accessMode;
	short resourceType;
	short nmPipeStatus;
	short actionToken;
	int reserved;
	short extendedAttributeErrorOffset;
	int extendedAttributeLength;
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public FileAttributes getFileAttribute() {
		return fileAttribute;
	}
	public void setFileAttribute(FileAttributes fileAttribute) {
		this.fileAttribute = fileAttribute;
	}
	public int getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(int creationTime) {
		this.creationTime = creationTime;
	}
	public int getFiledataSize() {
		return fileDataSize;
	}
	public void setFiledataSize(int fileDdataSize) {
		this.fileDataSize = fileDdataSize;
	}
	public short getAccessMode() {
		return accessMode;
	}
	public void setAccessMode(short accessMode) {
		this.accessMode = accessMode;
	}
	public short getResourceType() {
		return resourceType;
	}
	public void setResourceType(short resourceType) {
		this.resourceType = resourceType;
	}
	public short getNmPipeStatus() {
		return nmPipeStatus;
	}
	public void setNmPipeStatus(short nmPipeStatus) {
		this.nmPipeStatus = nmPipeStatus;
	}
	public short getActionToken() {
		return actionToken;
	}
	public void setActionToken(short actionTaken) {
		this.actionToken = actionTaken;
	}
	public int getReserved() {
		return reserved;
	}
	public void setReserved(int reserved) {
		this.reserved = reserved;
	}
	public short getExtendedAttributeErrorOffset() {
		return extendedAttributeErrorOffset;
	}
	public void setExtendedAttributeErrorOffset(short extendedAttributeErrorOffset) {
		this.extendedAttributeErrorOffset = extendedAttributeErrorOffset;
	}
	public int getExtendedAttributeLength() {
		return extendedAttributeLength;
	}
	public void setExtendedAttributeLength(int extendedAttributeLength) {
		this.extendedAttributeLength = extendedAttributeLength;
	}
	@Override
	public String toString(){
		return String.format("Trans2 Seconde Level : Open 2 Response\n" +
				"fid = 0x%s , fileAttribute = %s , creationTime = 0x%s\n" +
				"fileDataSize = 0x%s , accessMode = 0x%s , resrouceType = 0x%s\n" +
				"nmPipeStatus = 0x%s , actionToken = 0x%s , reserved = 0x%s\n" +
				"extendedAttributeErrorOffset = 0x%s , extendedAttributeLength = 0x%s\n",
				Integer.toHexString(this.fid) , this.fileAttribute, Integer.toHexString(this.creationTime),
				Integer.toHexString(this.fileDataSize), Integer.toHexString(this.accessMode), Integer.toHexString(this.resourceType),
				Integer.toHexString(this.nmPipeStatus), Integer.toHexString(this.actionToken) , Integer.toHexString(this.reserved),
				Integer.toHexString(this.extendedAttributeErrorOffset) , Integer.toHexString(this.extendedAttributeLength));
	}
}
