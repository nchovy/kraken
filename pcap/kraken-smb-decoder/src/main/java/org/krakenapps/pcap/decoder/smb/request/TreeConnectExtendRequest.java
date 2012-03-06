package org.krakenapps.pcap.decoder.smb.request;

import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class TreeConnectExtendRequest implements SmbData{

	boolean malformed = false;
	public final static short TREE_CONNECT_ANDX_DISCONNECT_TID = 0x0001;
	
	//following reserved bitmask is not used 
	public final static short RESERVED1 = 0x0002;
	public final static short RESREVED2 = (short) 0xFFFC;
	//param
	byte wordCount;
	byte andxCommand;
	byte andxReserved;
	short andxOffset;
	short flags;
	short passwordLength;
	//data
	short byteCount;
	byte []password;
	byte []pad;
	String path;
	String service;
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
	public short getPasswordLength() {
		return passwordLength;
	}
	public void setPasswordLength(short passwordLength) {
		this.passwordLength = passwordLength;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	public byte[] getPassword() {
		return password;
	}
	public void setPassword(byte[] password) {
		this.password = password;
	}
	public byte[] getPad() {
		return pad;
	}
	public void setPad(byte[] pad) {
		this.pad = pad;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public boolean isTreeConnectAndxDisconnectTid()
	{
		if((this.flags & TREE_CONNECT_ANDX_DISCONNECT_TID)== TREE_CONNECT_ANDX_DISCONNECT_TID)
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
		return String.format("First Level : Tree Connect Extend Request\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0x%s\n" +
				"flags = 0x%s , passwordLength = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"password = %s\n" +
				"path = %s\n" +
				"service = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.andxCommand), Integer.toHexString(this.andxReserved), Integer.toHexString(this.andxOffset),
				Integer.toHexString(this.flags) , Integer.toHexString(this.passwordLength),
				Integer.toHexString(this.byteCount),
				this.password.toString(),
				this.path,
				this.service);
	}
}