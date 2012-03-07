package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class SessionSetupANDXResponse implements SmbData{

	boolean malformed = false;
	byte wordCount; // 
	byte andxCommand;
	byte andxResrved;
	short andxoffset;
	short action;
	short byteCount; //
	byte []pad;
	String nativeOS;
	String nativeLanMan;
	String primaryDomain;
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
		return andxResrved;
	}
	public void setAndxResrved(byte andxResrved) {
		this.andxResrved = andxResrved;
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
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	public byte[] getPad() {
		return pad;
	}
	public void setPad(byte[] pad) {
		this.pad = pad;
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
	public String getPrimaryDomain() {
		return primaryDomain;
	}
	public void setPrimaryDomain(String primaryDomain) {
		this.primaryDomain = primaryDomain;
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
		return String.format("First Level : Session Setup Andx Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0x%s\n" +
				"action = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"NativeOS = %s\n" +
				"NativeLanMan = %s\n" +
				"PrimaryDomain = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.andxCommand) , Integer.toHexString(this.andxResrved) , Integer.toHexString(this.andxoffset),
				Integer.toHexString(this.action),
				Integer.toHexString(this.byteCount),
				this.nativeOS,
				this.nativeLanMan,
				this.primaryDomain);
	}
}
