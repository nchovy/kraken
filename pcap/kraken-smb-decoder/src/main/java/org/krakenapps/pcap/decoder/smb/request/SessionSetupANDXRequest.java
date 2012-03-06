package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class SessionSetupANDXRequest implements SmbData{

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
	short oemPasswordLen;
	short unicodePasswordLen;
	int reserved;
	int capabilities;
	//data
	short byteCount;
	byte[] oemPassword;
	byte[] unicodePassword;
	byte []pad;
	String accountName;
	String primaryDomain;
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
	public short getOemPasswordLen() {
		return oemPasswordLen;
	}
	public void setOemPasswordLen(short oemPasswordLen) {
		this.oemPasswordLen = oemPasswordLen;
	}
	public short getUnicodePasswordLen() {
		return unicodePasswordLen;
	}
	public void setUnicodePasswordLen(short unicodePasswordLen) {
		this.unicodePasswordLen = unicodePasswordLen;
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
	public byte[] getOemPassword() {
		return oemPassword;
	}
	public void setOemPassword(byte[] oemPassword) {
		this.oemPassword = oemPassword;
	}
	public byte[] getUnicodePassword() {
		return unicodePassword;
	}
	public void setUnicodePassword(byte[] unicodePassword) {
		this.unicodePassword = unicodePassword;
	}
	public byte[] getPad() {
		return pad;
	}
	public void setPad(byte[] pad) {
		this.pad = pad;
	}
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public String getPrimaryDomain() {
		return primaryDomain;
	}
	public void setPrimaryDomain(String primaryDomain) {
		this.primaryDomain = primaryDomain;
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
		return malformed;
	}
	@Override
	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}
	//page 316
	@Override
	public String toString(){
		return String.format("First Level : Session Setup Andx Extend Request\n" +
				"is Malformed = %s\n" +
				"wordCount = 0x%s\n" +
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0x%s\n" +
				"maxBufferSize = 0x%s , maxMpxCount = 0x%s , vcNumber = 0x%s\n" +
				"sessionKey = 0x%s , oemPasswordLen = 0x%s , unicodePasswordLen = 0x%s\n" +
				"reserved = 0x%s ,capabilities = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"oemPassword = %s\n" +
				"unicodePassword = %s\n" +
				"accountName = %s\n" +
				"primaryDomain = %x\n" +
				"native OS = %s\n" +
				"nativeLanMan = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.andxCommand) , Integer.toHexString(this.andxReserved) , Integer.toHexString(this.andxOffset),
				Integer.toHexString(this.maxBufferSize) , Integer.toHexString(this.maxMpxCount) , Integer.toHexString(this.vcNumber),
				Integer.toHexString(this.sessionKey) , Integer.toHexString(this.oemPasswordLen) , Integer.toHexString(this.unicodePasswordLen),
				Integer.toHexString(this.reserved) , Integer.toHexString(this.capabilities),
				Integer.toHexString(this.byteCount),
				this.oemPassword,
				this.unicodePassword,
				this.accountName,
				this.primaryDomain,
				this.nativeOS,
				this.nativeLanMan);
	}
}
