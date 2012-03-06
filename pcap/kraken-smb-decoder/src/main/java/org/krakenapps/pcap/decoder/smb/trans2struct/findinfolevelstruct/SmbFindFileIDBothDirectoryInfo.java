package org.krakenapps.pcap.decoder.smb.trans2struct.findinfolevelstruct;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.decoder.smb.rr.ExtFileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbFeaList;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbFindFileIDBothDirectoryInfo implements TransStruct {

	// added extension
	int nextEntryOffset;
	int fileIndex;
	long creationTime;
	long lastAccessTime;
	long lastWriteTime;
	long lastChangeTime;
	long endOfFile;
	long allocationSize;
	long endofFile;
	long allocateSize;
	ExtFileAttributes extFileAttributes;
	int fileNameLength;
	int eaSize;
	byte shortNameLength;
	byte reserved;
	String shortName;
	short reserved2;
	long fileID;
	String fileName;
	SmbFeaList feaList;

	public int getNextEntryOffset() {
		return nextEntryOffset;
	}

	public void setNextEntryOffset(int nextEntryOffset) {
		this.nextEntryOffset = nextEntryOffset;
	}

	public int getFileIndex() {
		return fileIndex;
	}

	public void setFileIndex(int fileIndex) {
		this.fileIndex = fileIndex;
	}

	public long getEndofFile() {
		return endofFile;
	}

	public void setEndofFile(long endofFile) {
		this.endofFile = endofFile;
	}

	public long getAllocateSize() {
		return allocateSize;
	}

	public void setAllocateSize(long allocateSize) {
		this.allocateSize = allocateSize;
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

	public long getEndOfFile() {
		return endOfFile;
	}

	public void setEndOfFile(long endOfFile) {
		this.endOfFile = endOfFile;
	}

	public long getAllocationSize() {
		return allocationSize;
	}

	public void setAllocationSize(long allocationSize) {
		this.allocationSize = allocationSize;
	}

	public int getFileNameLength() {
		return fileNameLength;
	}

	public void setFileNameLength(int fileNameLength) {
		this.fileNameLength = fileNameLength;
	}

	public ExtFileAttributes getExtFileAttributes() {
		return extFileAttributes;
	}

	public void setExtFileAttributes(ExtFileAttributes extFileAttributes) {
		this.extFileAttributes = extFileAttributes;
	}

	public int getFilenameLength() {
		return fileNameLength;
	}

	public void setFilenameLength(int filenameLength) {
		this.fileNameLength = filenameLength;
	}

	public int getEaSize() {
		return eaSize;
	}

	public void setEaSize(int eaSize) {
		this.eaSize = eaSize;
	}

	public byte getShortNameLength() {
		return shortNameLength;
	}

	public void setShortNameLength(byte shortNameLength) {
		this.shortNameLength = shortNameLength;
	}

	public byte getReserved() {
		return reserved;
	}

	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public short getReserved2() {
		return reserved2;
	}

	public void setReserved2(short reserved2) {
		this.reserved2 = reserved2;
	}

	public long getFileID() {
		return fileID;
	}

	public void setFileID(long fileID) {
		this.fileID = fileID;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public SmbFeaList getFeaList() {
		return feaList;
	}

	public void setFeaList(SmbFeaList feaList) {
		this.feaList = feaList;
	}

	public TransStruct parse(Buffer b, SmbSession session) {
		nextEntryOffset = ByteOrderConverter.swap(b.getInt());
		fileIndex = ByteOrderConverter.swap(b.getInt());
		creationTime = ByteOrderConverter.swap(b.getLong());
		lastAccessTime = ByteOrderConverter.swap(b.getLong());
		lastWriteTime = ByteOrderConverter.swap(b.getLong());
		lastChangeTime = ByteOrderConverter.swap(b.getLong());
		endofFile = ByteOrderConverter.swap(b.getLong());
		allocateSize = ByteOrderConverter.swap(b.getLong());
		extFileAttributes = ExtFileAttributes.parse(ByteOrderConverter.swap(b
				.getInt()));
		fileNameLength = ByteOrderConverter.swap(b.getInt());
		eaSize = ByteOrderConverter.swap(b.getInt());
		shortNameLength = b.get();
		reserved = b.get();
		shortName = NetBiosNameCodec.readOemName(b, 12);
		reserved2 = ByteOrderConverter.swap(b.getShort());
		fileID = ByteOrderConverter.swap(b.getLong());
		fileName = NetBiosNameCodec.readSmbUnicodeName(b, fileNameLength);
		feaList = new SmbFeaList();
		feaList.parse(b);
		b.rewind();
		b.skip(nextEntryOffset);
		return this;
	}
	@Override
	public String toString(){
		return String.format("Third Level Structure : Smb Find File ID Both directory Info\n" +
				"nextEntryOffset = 0x%s , fileIndex = 0x%s , creationTime = 0x%s\n" +
				"lastAccessTime = 0x%s,  lastWriteTime = 0x%s, lastChageTime = 0x%s\n" +
				"endOfFile = 0x%s , allocationSize = 0x%s , extfileattributes = %s\n" +
				"fileNameLength = %s , eaSize = 0x%s , shortNameLength  = 0x%s\n" +
				"reserved1 = 0x%s , shortName = %s\n" +
				"reserved2 = 0x%s , fileID = 0x%s , fileName = %s\n" +
				"feaList\n" +
				"%s",
				Integer.toHexString(this.nextEntryOffset) , Integer.toHexString(this.fileIndex) , Long.toHexString(this.creationTime),
				Long.toHexString(this.lastAccessTime) , Long.toHexString(this.lastWriteTime) , Long.toHexString(this.lastChangeTime),
				Long.toHexString(this.endOfFile) , Long.toHexString(this.allocationSize) , this.extFileAttributes,
				Integer.toHexString(this.fileNameLength) , Integer.toHexString(this.eaSize), Integer.toHexString(this.shortNameLength),
				Integer.toHexString(reserved), this.shortName,
				Integer.toHexString(this.reserved2) , Long.toHexString(this.fileID) , this.fileName,
				this.feaList);
	}
}
