package org.krakenapps.pcap.decoder.smb.trans2req;

import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbFeaList;

public class Open2Resquest implements TransData{
	short subcommand;
	//param
	short flags;
	short accessMode;
	short searchAttribute;
	FileAttributes fileAttribute;
	int creationTime;
	short openMode;
	int allocationSize;
	byte []Reserved;
	String fileName;
	SmbFeaList extendedAttributeList;
	
	public short getSearchAttribute() {
		return searchAttribute;
	}
	public void setSearchAttribute(short searchAttribute) {
		this.searchAttribute = searchAttribute;
	}
	public SmbFeaList getExtendedAttributeList() {
		return extendedAttributeList;
	}
	public void setExtendedAttributeList(SmbFeaList extendedAttributeList) {
		this.extendedAttributeList = extendedAttributeList;
	}
	public void setAcllocationSize(int allocationSize) {
		this.allocationSize = allocationSize;
	}
	public short getSubcommand() {
		return subcommand;
	}
	public void setSubcommand(short subcommand) {
		this.subcommand = subcommand;
	}
	public short getFlags() {
		return flags;
	}
	public void setFlags(short flags) {
		this.flags = flags;
	}
	public short getAccessMode() {
		return accessMode;
	}
	public void setAccessMode(short accessMode) {
		this.accessMode = accessMode;
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
	public short getOpenMode() {
		return openMode;
	}
	public void setOpenMode(short openMode) {
		this.openMode = openMode;
	}
	public int getAllocationSize() {
		return allocationSize;
	}
	public void setAllocationSize(int allocationSize) {
		this.allocationSize = allocationSize;
	}
	public byte[] getReserved() {
		return Reserved;
	}
	public void setReserved(byte[] reserved) {
		Reserved = reserved;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	@Override
	public String toString(){
		return String.format("Trans2 Second Level : Open2 Request\n" +
				"subCommand = 0x%s\n" +
				"flags = 0x%s, accessMode = 0x%s , searchAttributes = 0x%s\n" +
				"fileAttributes = %s , creationTime = 0x%s, openMode = 0x%s\n" +
				"allocationSoze = 0x%s , reserved = %s\n" +
				"fileName = %s\n" +
				"extendedAttributeList = %s\n",
				Integer.toHexString(this.subcommand),
				Integer.toHexString(this.flags), Integer.toHexString(this.accessMode), Integer.toHexString(this.searchAttribute),
				this.fileAttribute, Integer.toHexString(this.creationTime) , Integer.toHexString(this.openMode),
				Integer.toHexString(this.allocationSize) , this.Reserved.toString(),
				this.fileName,
				this.extendedAttributeList);
	}
}
