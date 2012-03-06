package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class OpenANDXRequest implements SmbData{
	boolean malformed = false;
	//param
	byte wordCount;
	byte andxCommand;
	byte andxReserved;
	short andxOffset;
	short flags;
	short accessMode;
	FileAttributes searchAttrs;
	FileAttributes fileAttrs; 
	int creationTime;
	short openMode;
	int allocationSize;
	int timeout;
	byte []Reserved = new byte[4];
	//data
	short byteCount;
	String fileName;
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
	public FileAttributes getSearchAttrs() {
		return searchAttrs;
	}
	public void setSearchAttrs(FileAttributes searchAttrs) {
		this.searchAttrs = searchAttrs;
	}
	public FileAttributes getFileAttrs() {
		return fileAttrs;
	}
	public void setFileAttrs(FileAttributes fileAttrs) {
		this.fileAttrs = fileAttrs;
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
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public byte[] getReserved() {
		return Reserved;
	}
	public void setReserved(byte[] reserved) {
		Reserved = reserved;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public boolean isFlagOpenQueryInformation()
	{
		if((flags & SMB_OPEN_QUERY_INFORMATION) == SMB_OPEN_QUERY_INFORMATION)
			return true;
		else
			return false;
	}
	public boolean isFlagOpenOplock()
	{
		if((flags & SMB_OPEN_OPLOCK) == SMB_OPEN_OPLOCK)
			return true;
		else
			return false;
	}
	public boolean isFlagOpenOpBatch()
	{
		if( (flags & SMB_OPEN_OPLOCK) == SMB_OPEN_OPLOCK)
			return true;
		else
			return false;
	}
	public boolean isFlagOpenExtendedResponse()
	{
		if( (flags & SMB_OPEN_EXTENDED_RESPONSE) == SMB_OPEN_EXTENDED_RESPONSE)
			return true;
		else
			return false;
	}
	public boolean isFlagResreved()
	{
		if( (flags & RESERVED) == RESERVED)
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
		return String.format("First Level : OpenAndx Request\n"+
				"isMalformed =%s\n"+
				"wordCount = 0x%s\n" +
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0x%s\n" +
				"flags = 0x%s, accessMode = 0x%s , searchAttr = %s\n" +
				"fileattrs = %s , creationTime = 0x%s , openMode = 0x%s\n" +
				"allocationSize = 0x%s , timeOut = 0x%s , reserved = %s\n" +
				"byteCount = 0x%s\n" +
				"fileName = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.andxCommand), Integer.toHexString(this.andxReserved) , Integer.toHexString(this.andxOffset),
				Integer.toHexString(this.flags) , Integer.toHexString(this.accessMode) , this.searchAttrs,
				this.fileAttrs , Integer.toHexString(this.creationTime) , Integer.toHexString(this.openMode),
				Integer.toHexString(this.allocationSize) , Integer.toHexString(this.timeout), this.Reserved.toString(),
				Integer.toHexString(this.byteCount),
				this.fileName);
	}
}
