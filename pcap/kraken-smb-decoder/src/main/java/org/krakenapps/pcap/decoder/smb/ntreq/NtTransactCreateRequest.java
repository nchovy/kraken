package org.krakenapps.pcap.decoder.smb.ntreq;

import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.rr.ExtFileAttributes;

public class NtTransactCreateRequest implements TransData{

	int flags;
	int rootDirectoryFid;
	int desiredAccess;
	Long allocationSize;
	ExtFileAttributes extFileAttributes;//b4yte
	int shareAccess;
	int createDisposition;
	int createOptions;
	int securityDescriptorLength;
	int eaLength;
	int nameLength;
	int impersonationLevel;
	byte securityFlags;
	String name;
	
	//data
	byte []securityDescriptor;
	byte []extendedAttribytes;
	public int getFlags() {
		return flags;
	}
	public void setFlags(int flags) {
		this.flags = flags;
	}
	public int getRootDirectoryFid() {
		return rootDirectoryFid;
	}
	public void setRootDirectoryFid(int rootDirectoryFid) {
		this.rootDirectoryFid = rootDirectoryFid;
	}
	public int getDesiredAccess() {
		return desiredAccess;
	}
	public void setDesiredAccess(int desiredAccess) {
		this.desiredAccess = desiredAccess;
	}
	public Long getAllocationSize() {
		return allocationSize;
	}
	public void setAllocationSize(Long allocationSize) {
		this.allocationSize = allocationSize;
	}
	public ExtFileAttributes getExtFileAttributes() {
		return extFileAttributes;
	}
	public void setExtFileAttributes(ExtFileAttributes extFileAttributes) {
		this.extFileAttributes = extFileAttributes;
	}
	public int getShareAccess() {
		return shareAccess;
	}
	public void setShareAccess(int shareAccess) {
		this.shareAccess = shareAccess;
	}
	public int getCreateDisposition() {
		return createDisposition;
	}
	public void setCreateDisposition(int createDisposition) {
		this.createDisposition = createDisposition;
	}
	public int getCreateOptions() {
		return createOptions;
	}
	public void setCreateOptions(int createOptions) {
		this.createOptions = createOptions;
	}
	public int getSecurityDescriptorLength() {
		return securityDescriptorLength;
	}
	public void setSecurityDescriptorLength(int securityDescriptorLength) {
		this.securityDescriptorLength = securityDescriptorLength;
	}
	public int getEaLength() {
		return eaLength;
	}
	public void setEaLength(int eaLength) {
		this.eaLength = eaLength;
	}
	public int getNameLength() {
		return nameLength;
	}
	public void setNameLength(int nameLength) {
		this.nameLength = nameLength;
	}
	public int getImpersonationLevel() {
		return impersonationLevel;
	}
	public void setImpersonationLevel(int impersonationLevel) {
		this.impersonationLevel = impersonationLevel;
	}
	public byte getSecurityFlags() {
		return securityFlags;
	}
	public void setSecurityFlags(byte securityFlags) {
		this.securityFlags = securityFlags;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public byte[] getSecurityDescriptor() {
		return securityDescriptor;
	}
	public void setSecurityDescriptor(byte[] securityDescriptor) {
		this.securityDescriptor = securityDescriptor;
	}
	public byte[] getExtendedAttribytes() {
		return extendedAttribytes;
	}
	public void setExtendedAttribytes(byte[] extendedAttribytes) {
		this.extendedAttribytes = extendedAttribytes;
	}
	public String toString(){
		return String.format("Second Level : Nt Transact Create Request\n" +
				"flags = 0x%s , rootDirectoryFid = 0x%s , desiredAccess = 0x%s , allocationSize = 0x%s\n"+
				"extFileAttribute = %s , shareAccess = 0x%s , createDisposition = 0x%s , createOptions = 0x%s\n"+
				"securityDescriptorLength = 0x%s , eaLength = 0x%s , namelength = 0x%s ,impersonationlevel = 0x%s\n"+
				"securityFlags = 0x%s , name = %s\n" , 
				Integer.toHexString(this.flags) , Integer.toHexString(this.rootDirectoryFid) , Integer.toHexString(this.desiredAccess) , Long.toHexString(this.allocationSize),
				this.extFileAttributes , Integer.toHexString(this.shareAccess) , Integer.toHexString(this.createDisposition) , Integer.toHexString(this.createOptions),
				Integer.toHexString(this.securityDescriptorLength) , Integer.toHexString(this.eaLength) , Integer.toHexString(this.nameLength) , Integer.toHexString(this.impersonationLevel),
				Integer.toHexString(this.securityFlags) , this.name);
	}
}
