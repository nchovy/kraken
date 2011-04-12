package org.krakenapps.pcap.decoder.smb.response;

import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class TreeConnectANDXExtendResponse implements SmbData{
	
	boolean malformed = false;
	static final short SMB_SUPPORT_SEAERCH_BITS = 0x0001;
	static final short SMB_SHARE_IS_IN_DFS = 0x0002;
	static final short SMB_CSC_MASK = 0x000C;
	static final short SMB_UNIQUE_FILE_NAME = 0x0010;
	static final short SMB_EXTNEDED_SIGNATURES = 0x0020;
	
	byte WordCount; 
	byte AndxCommand;
	byte AndxReserved;
	short andxOffset;
	short optionalSupport;
	int maximalShareAccessRight;
	int guestMaximalShareAccessRight;
	
	short byteCount; 
	String service;
	String nativeFileSystem;
	public byte getWordCount() {
		return WordCount;
	}
	public void setWordCount(byte wordCount) {
		WordCount = wordCount;
	}
	public byte getAndxCommand() {
		return AndxCommand;
	}
	public void setAndxCommand(byte andxCommand) {
		AndxCommand = andxCommand;
	}
	public byte getAndxReserved() {
		return AndxReserved;
	}
	public void setAndxReserved(byte andxReserved) {
		AndxReserved = andxReserved;
	}
	public short getAndxOffset() {
		return andxOffset;
	}
	public void setAndxOffset(short andxOffset) {
		this.andxOffset = andxOffset;
	}
	public short getOptionalSupport() {
		return optionalSupport;
	}
	public void setOptionalSupport(short optionalSupport) {
		this.optionalSupport = optionalSupport;
	}
	public int getMaximalShareAccessRight() {
		return maximalShareAccessRight;
	}
	public void setMaximalShareAccessRight(int maximalShareAccessRight) {
		this.maximalShareAccessRight = maximalShareAccessRight;
	}
	public int getGuestMaximalShareAccessRight() {
		return guestMaximalShareAccessRight;
	}
	public void setGuestMaximalShareAccessRight(int guestMaximalShareAccessRight) {
		this.guestMaximalShareAccessRight = guestMaximalShareAccessRight;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getNativeFileSystem() {
		return nativeFileSystem;
	}
	public void setNativeFileSystem(String nativeFileSystem) {
		this.nativeFileSystem = nativeFileSystem;
	}

	public boolean isOptionSupportSearchBits()
	{
		if((this.optionalSupport & SMB_SUPPORT_SEAERCH_BITS) == SMB_SUPPORT_SEAERCH_BITS)
			return true;
		else
			return false;
	}
	public boolean isOptionShareIsInDfs()
	{
		if((this.optionalSupport & SMB_SHARE_IS_IN_DFS) == SMB_SHARE_IS_IN_DFS)
			return true;
		else
			return false;
	}
	public boolean isOptionCscCacheAutoReint()
	{
		if(((this.optionalSupport & SMB_CSC_MASK)>>2 ) == 1)
			return true;
		else
			return false;
	}
	public boolean isOptionCscCacheVdo()
	{
		if(((this.optionalSupport & SMB_CSC_MASK)>>2 ) == 2)
			return true;
		else
			return false; 
	}
	public boolean isOptionCscNoCashing()
	{
		if(((this.optionalSupport & SMB_CSC_MASK)>>2 ) == 3)
			return true;
		else
			return false; 
	}
	public boolean isOptionCscCacheManualReint()
	{
		if(((this.optionalSupport & SMB_CSC_MASK)>>2 ) == 0)
			return true;
		else
			return false; 
	}
	public boolean isOptionUniqueFileName()
	{
		if((this.optionalSupport & SMB_UNIQUE_FILE_NAME) == SMB_UNIQUE_FILE_NAME)
			return true;
		else
			return false;
	}
	public boolean isOptionExtendedSugnatures()
	{
		if((this.optionalSupport & SMB_EXTNEDED_SIGNATURES) == SMB_EXTNEDED_SIGNATURES)
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
		return String.format("First Level : Tree Connect Andx Extend Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"andxCommand = 0x%s, andxReserved = 0x%s, andxOffset = 0x%s\n" +
				"optionalSupport = 0x%s , maximalShareAccessRight = 0x%s , guestMaximalShareAccessRight = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"service = %s\n" +
				"nativeFileSystem = %s\n",
				this.malformed,
				Integer.toHexString(this.WordCount),
				Integer.toHexString(this.AndxCommand) , Integer.toHexString(this.AndxReserved) , Integer.toHexString(this.andxOffset),
				Integer.toHexString(this.optionalSupport) , Integer.toHexString(this.maximalShareAccessRight) , Integer.toHexString(this.guestMaximalShareAccessRight),
				Integer.toHexString(this.byteCount),
				this.service,
				this.nativeFileSystem);
	}
}
