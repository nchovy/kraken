package org.krakenapps.pcap.decoder.smb.response;

import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class SessionSetupANDXExtendResponse implements SmbData{

	boolean malformed = false;
	byte wordCount; // 
	byte andxCommand;
	byte andxReserved;
	short andxoffset;
	short action;
	short securityBlobLenth;
	
	short byteCount; //
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
	public byte getAndxResrved() {
		return andxReserved;
	}
	public void setAndxResrved(byte andxResrved) {
		this.andxReserved = andxResrved;
	}
	public short getAndxoffset() {
		return andxoffset;
	}
	public void setAndxoffset(short andxoffset) {
		this.andxoffset = andxoffset;
	}
	public short getAction() {
		return action;
	}
	public void setAction(short action) {
		this.action = action;
	}
	public short getSecurityBlobLenth() {
		return securityBlobLenth;
	}
	public void setSecurityBlobLenth(short securityBlobLenth) {
		this.securityBlobLenth = securityBlobLenth;
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
		return String.format("First Level : Session Setup Andx Extend Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0x0%s\n" +
				"action = 0x%s , securityBlobLength = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"securityBlob = %s\n" +
				"NativeOS = %s\n" +
				"NativeLanMan = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.andxCommand) , Integer.toHexString(this.andxReserved) ,Integer.toHexString(this.andxoffset),
				Integer.toHexString(this.action) , Integer.toHexString(this.securityBlobLenth),
				Integer.toHexString(this.byteCount),
				this.securityBlob,
				this.nativeOS,
				this.nativeLanMan);
	}
}
