package org.krakenapps.pcap.decoder.smb.request;

import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class SessionSetupANDXExtendRequest implements SmbData{

	boolean malformed = false;
	//param
	byte wordCount;
	byte andxCommand;
	byte andxReserved;
	short andxOffset;
	short maxBufferSize;
	short maxMpxCount;
	short vcNumber;
	int sessionKey;
	short securityBlobLenth;
	int reserved;
	int capabilities;
	//data
	short byteCount;
	byte []securityBlob;
	String nativeOS;
	String nativeLanMan;
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
	public short getMaxBufferSize() {
		return maxBufferSize;
	}
	public void setMaxBufferSize(short maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
	}
	public short getMaxMpxCount() {
		return maxMpxCount;
	}
	public void setMaxMpxCount(short maxMpxCount) {
		this.maxMpxCount = maxMpxCount;
	}
	public short getVcNumber() {
		return vcNumber;
	}
	public void setVcNumber(short vcNumber) {
		this.vcNumber = vcNumber;
	}
	public int getSessionKey() {
		return sessionKey;
	}
	public void setSessionKey(int sessionKey) {
		this.sessionKey = sessionKey;
	}
	public short getSecurityBlobLenth() {
		return securityBlobLenth;
	}
	public void setSecurityBlobLenth(short securityBlobLenth) {
		this.securityBlobLenth = securityBlobLenth;
	}
	public int getReserved() {
		return reserved;
	}
	public void setReserved(int reserved) {
		this.reserved = reserved;
	}
	public int getCapabilities() {
		return capabilities;
	}
	public void setCapabilities(int capabilities) {
		this.capabilities = capabilities;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	public byte[] getSecurityBlob() {
		return securityBlob;
	}
	public void setSecurityBlob(byte[] securityBlob) {
		this.securityBlob = securityBlob;
	}
	public String getNativeOS() {
		return nativeOS;
	}
	public void setNativeOS(String nativeOS) {
		this.nativeOS = nativeOS;
	}
	public String getNativeLanMan() {
		return nativeLanMan;
	}
	public void setNativeLanMan(String nativeLanMan) {
		this.nativeLanMan = nativeLanMan;
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
		return String.format("First Level : Session Setup Andx Extend Request\n" +
				"is Malformed = %s\n" +
				"wordCount = 0x%s\n" +
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0x%s\n" +
				"maxBufferSize = 0x%s , maxMpxCount = 0x%s , vcNumber = 0x%s\n" +
				"sessionKey = 0x%s , securityBlobkLength = 0x%s , reserved = 0x%s ,capabilities = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"securityBlob = %s\n" +
				"native OS = %s\n" +
				"nativeLanMan = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.andxCommand) , Integer.toHexString(this.andxReserved) , Integer.toHexString(this.andxOffset),
				Integer.toHexString(this.maxBufferSize) , Integer.toHexString(this.maxMpxCount) , Integer.toHexString(this.vcNumber),
				Integer.toHexString(this.sessionKey) , Integer.toHexString(this.securityBlobLenth) , Integer.toHexString(this.reserved) , Integer.toHexString(this.capabilities),
				Integer.toHexString(this.byteCount),
				this.securityBlob,
				this.nativeOS,
				this.nativeLanMan);
	}
}
