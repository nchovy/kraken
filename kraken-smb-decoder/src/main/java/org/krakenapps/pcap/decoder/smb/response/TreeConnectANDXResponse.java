package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class TreeConnectANDXResponse implements SmbData{
	
	boolean malformed = false;
	static final short SMB_SUPPORT_SEAERCH_BITS = 0x0001;
	static final short SMB_SHARE_IS_IN_DFS = 0x0002;
	byte WordCount; // it must 0x00
	byte AndxCommand;
	byte AndxReserved;
	short andxOffset;
	short optionalSupport;
	short byteCount; // it must 0x0000
	String service;
	byte []pad;
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
	public byte[] getPad() {
		return pad;
	}
	public void setPad(byte[] pad) {
		this.pad = pad;
	}
	public String getNativeFileSystem() {
		return nativeFileSystem;
	}
	public void setNativeFileSystem(String nativeFileSystem) {
		this.nativeFileSystem = nativeFileSystem;
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
		return String.format("First Level : Tree Connect Andx Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0x%s\n" +
				"optionalSupport = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"service = %s\n",
				"NativeFile System = %s\n",
				this.malformed,
				Integer.toHexString(this.WordCount),
				Integer.toHexString(this.AndxCommand), Integer.toHexString(this.AndxReserved) , Integer.toHexString(this.andxOffset),
				Integer.toHexString(this.optionalSupport),
				Integer.toHexString(this.byteCount),
				this.service,
				this.nativeFileSystem);
	}
}
