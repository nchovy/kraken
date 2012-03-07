package org.krakenapps.pcap.decoder.smb.trans2struct.queryinfostruct;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbInfoQueryEaSize implements TransStruct{

	short creationDate;
	short creationTime;
	short lastAccessDate;
	short lastAccessTime;
	short lastWriteDate;
	short lastWriteTime;
	int fileDataSize;
	int allocationSize; 
	FileAttributes attributes;
	int eaSize;
	
	public short getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(short creationDate) {
		this.creationDate = creationDate;
	}

	public short getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(short creationTime) {
		this.creationTime = creationTime;
	}

	public short getLastAccessDate() {
		return lastAccessDate;
	}

	public void setLastAccessDate(short lastAccessDate) {
		this.lastAccessDate = lastAccessDate;
	}

	public short getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(short lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public short getLastWriteDate() {
		return lastWriteDate;
	}

	public void setLastWriteDate(short lastWriteDate) {
		this.lastWriteDate = lastWriteDate;
	}

	public short getLastWriteTime() {
		return lastWriteTime;
	}

	public void setLastWriteTime(short lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}

	public int getFileDataSize() {
		return fileDataSize;
	}

	public void setFileDataSize(int fileDataSize) {
		this.fileDataSize = fileDataSize;
	}

	public int getAllocationSize() {
		return allocationSize;
	}

	public void setAllocationSize(int allocationSize) {
		this.allocationSize = allocationSize;
	}

	public FileAttributes getAttributes() {
		return attributes;
	}

	public void setAttributes(FileAttributes attributes) {
		this.attributes = attributes;
	}

	public int getEaSize() {
		return eaSize;
	}

	public void setEaSize(int eaSize) {
		this.eaSize = eaSize;
	}

	@Override
	public TransStruct parse(Buffer b , SmbSession session) {
		creationDate = ByteOrderConverter.swap(b.getShort());
		creationTime = ByteOrderConverter.swap(b.getShort());
		lastAccessDate = ByteOrderConverter.swap(b.getShort());
		lastAccessTime = ByteOrderConverter.swap(b.getShort());
		lastWriteDate = ByteOrderConverter.swap(b.getShort());
		lastWriteTime = ByteOrderConverter.swap(b.getShort());
		fileDataSize = ByteOrderConverter.swap(b.getInt());
		allocationSize = ByteOrderConverter.swap(b.getInt());
		attributes = FileAttributes.parse(ByteOrderConverter.swap(b.getShort()));
		eaSize =ByteOrderConverter.swap(b.getInt()); 
		return this;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
