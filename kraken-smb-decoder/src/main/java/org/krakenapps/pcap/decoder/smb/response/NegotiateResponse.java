package org.krakenapps.pcap.decoder.smb.response;

import org.krakenapps.pcap.decoder.smb.rr.Capability;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class NegotiateResponse implements SmbData {

	boolean malformed = false;
	// Security mode
	public final static byte NEGOTIATE_USER_SECURITY = 0x01;
	public final static byte NEGOTIATE_ENCRYPT_PASSWORDS = 0x02;
	public final static byte NEGOTIATE_SECURITY_SIGNATURES_ENABLE = 0x04;
	public final static byte NEGOTIATE_SECURITY_SIGNATURES_REQUIRED = 0x08;
	public final static byte RESERVED = (byte) 0xF0;

	byte wordCount;
	short dialectIndex;
	short byteCount;
	byte[] challenge;
	String domainName;
	String serverName; // i don't know when i use it
	// //////////////////////////////////// other dialect
	// add NT Lan Manager follow
	byte sercurityMode;
	short maxMpxCount;
	short maxNumberVcs;
	int maxBufferSize;
	int maxRawSize;
	int sessionKey;
	int capabilities;
	long systemTime;
	short serverTimeZone;
	byte challengeLenghth;

	public byte getWordCount() {
		return wordCount;
	}

	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}

	public short getDialectIndex() {
		return dialectIndex;
	}

	public void setDialectIndex(short dialectIndex) {
		this.dialectIndex = dialectIndex;
	}

	public short getByteCount() {
		return byteCount;
	}

	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}

	public byte[] getChallenge() {
		return challenge;
	}

	public void setChallenge(byte[] challenge) {
		this.challenge = challenge;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public byte getSercurityMode() {
		return sercurityMode;
	}

	public void setSercurityMode(byte sercurityMode) {
		this.sercurityMode = sercurityMode;
	}

	public short getMaxMpxCount() {
		return maxMpxCount;
	}

	public void setMaxMpxCount(short maxMpxCount) {
		this.maxMpxCount = maxMpxCount;
	}

	public short getMaxNumberVcs() {
		return maxNumberVcs;
	}

	public void setMaxNumberVcs(short maxNumberVcs) {
		this.maxNumberVcs = maxNumberVcs;
	}

	public int getMaxBufferSize() {
		return maxBufferSize;
	}

	public void setMaxBufferSize(int maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
	}

	public int getMaxRawSize() {
		return maxRawSize;
	}

	public void setMaxRawSize(int maxRawSize) {
		this.maxRawSize = maxRawSize;
	}

	public int getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(int sessionKey) {
		this.sessionKey = sessionKey;
	}

	public int getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(int capabilities) {
		this.capabilities = capabilities;
	}

	public long getSystemTime() {
		return systemTime;
	}

	public void setSystemTime(long systemTime) {
		this.systemTime = systemTime;
	}

	public short getServerTimeZone() {
		return serverTimeZone;
	}

	public void setServerTimeZone(short serverTimeZone) {
		this.serverTimeZone = serverTimeZone;
	}

	public byte getChallengeLenghth() {
		return challengeLenghth;
	}

	public void setChallengeLenghth(byte challengeLenghth) {
		this.challengeLenghth = challengeLenghth;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public boolean isUserSecurity() {
		return ((sercurityMode & NEGOTIATE_USER_SECURITY) == NEGOTIATE_USER_SECURITY);
	}

	public boolean isEncryptPasswords() {
		return ((sercurityMode & NEGOTIATE_ENCRYPT_PASSWORDS) == NEGOTIATE_ENCRYPT_PASSWORDS);
	}

	public boolean isSecuritySignaturesEnable() {
		return ((sercurityMode & NEGOTIATE_SECURITY_SIGNATURES_ENABLE) == NEGOTIATE_SECURITY_SIGNATURES_ENABLE);
	}

	public boolean isSecuritySignaruesRequired() {
		return ((sercurityMode & NEGOTIATE_SECURITY_SIGNATURES_REQUIRED) == NEGOTIATE_SECURITY_SIGNATURES_REQUIRED);
	}

	public boolean isReserved() {
		return ((sercurityMode & RESERVED) == RESERVED);
	}

	// capability
	public boolean isCapRawMode() {
		return ((this.capabilities & Capability.CAP_RAW_MODE.getCapability()) == Capability.CAP_RAW_MODE
				.getCapability());
	}

	public boolean isCapMpxMode() {
		return ((this.capabilities & Capability.CAP_MPX_MODE.getCapability()) == Capability.CAP_MPX_MODE
				.getCapability());
	}

	public boolean isCapUnicode() {
		return ((this.capabilities & Capability.CAP_UNICODE.getCapability()) == Capability.CAP_UNICODE
				.getCapability());
	}

	public boolean isCaplargeFiles() {
		return ((this.capabilities & Capability.CAP_LARGE_FILES.getCapability()) == Capability.CAP_LARGE_FILES
				.getCapability());
	}

	public boolean isCapNtSmbs() {
		return ((this.capabilities & Capability.CAP_NT_SMBS.getCapability()) == Capability.CAP_NT_SMBS
				.getCapability());
	}

	public boolean isCapRpcRemoteApis() {
		return ((this.capabilities & Capability.CAP_RPC_REMOTE_APIS.getCapability()) == Capability.CAP_RPC_REMOTE_APIS
				.getCapability());
	}

	public boolean isCapStatus32() {
		return ((this.capabilities & Capability.CAP_STATUS32.getCapability()) == Capability.CAP_STATUS32
				.getCapability());
	}

	public boolean isLevel2Oplocks() {
		return ((this.capabilities & Capability.CAP_LEVEL_II_OPLOCKS
				.getCapability()) == Capability.CAP_LEVEL_II_OPLOCKS
				.getCapability());
	}

	public boolean isCapLockAndRead() {
		return ((this.capabilities & Capability.CAP_LOCK_AND_READ.getCapability()) == Capability.CAP_LOCK_AND_READ
				.getCapability());
	}

	public boolean isCapNtFind() {
		return ((this.capabilities & Capability.CAP_NT_FIND.getCapability()) == Capability.CAP_NT_FIND
				.getCapability());
	}

	public boolean isCapDfs() {
		return ((this.capabilities & Capability.CAP_DFS.getCapability()) == Capability.CAP_DFS
				.getCapability());
	}

	public boolean isCapInforlevelpassThru() {
		return ((this.capabilities & Capability.CAP_INFOLEVEL_PASSTHRU
				.getCapability()) == Capability.CAP_INFOLEVEL_PASSTHRU
				.getCapability());
	}

	public boolean isCapLargeReadx() {
		return ((this.capabilities & Capability.CAP_LARGE_READX.getCapability()) == Capability.CAP_LARGE_READX
				.getCapability());
	}

	public boolean isCapLargeWritex() {
		return ((this.capabilities & Capability.CAP_LARGE_WRITEX.getCapability()) == Capability.CAP_LARGE_WRITEX
				.getCapability());
	}

	public boolean isCapLwio() {
		return ((this.capabilities & Capability.CAP_LWIO.getCapability()) == Capability.CAP_LWIO
				.getCapability());
	}

	public boolean isCapUnix() {
		return ((this.capabilities & Capability.CAP_UNIX.getCapability()) == Capability.CAP_UNIX
				.getCapability());
	}

	public boolean isCapCompressedData() {
		return  ((this.capabilities & Capability.CAP_COMPRESSED_DATA.getCapability()) == Capability.CAP_COMPRESSED_DATA
				.getCapability());
	}

	public boolean isCapDynamicReauth() {
		return (this.capabilities & Capability.CAP_DYNAMIC_REAUTH
				.getCapability()) == Capability.CAP_DYNAMIC_REAUTH
				.getCapability();
	}


	public boolean isCapPersistentHandles() {
		return((this.capabilities & Capability.CAP_PERSISTENT_HANDLES
				.getCapability()) == Capability.CAP_PERSISTENT_HANDLES
				.getCapability());
	}

	public boolean isCapExtendedSecurity() {
		return((this.capabilities & Capability.CAP_EXTENDED_SECURITY
				.getCapability()) == Capability.CAP_EXTENDED_SECURITY
				.getCapability());
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
		return String.format("First Level : Negotiate Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"dialectIndex = 0x%s\n" +
				"byteCount = 0x%s\n" +
				"challenge = %s\n" +
				"domainName = %s\n" +
				"serverName = %s\n" +
				"securityMode = 0x%s , maxMpxCount = 0x%s , maxNumberVcs = 0x%s\n" +
				"maxBufferSize = 0x%s , maxRawSize = 0x%s , sessionKey = 0x%s\n" +
				"capabilities = 0x%s , systemTime = 0x%s , serverTimeZone = 0x%s , challengeLength = 0x%s",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.dialectIndex),
				Integer.toHexString(this.byteCount),
				this.challenge.toString(),
				this.domainName,
				this.serverName,
				Integer.toHexString(this.sercurityMode), Integer.toHexString(this.maxMpxCount) , Integer.toHexString(this.maxNumberVcs),
				Integer.toHexString(this.maxBufferSize) , Integer.toHexString(this.maxRawSize) , Integer.toHexString(this.sessionKey),
				Integer.toHexString(this.capabilities), Long.toHexString(this.systemTime) , Integer.toHexString(this.serverTimeZone) , Integer.toHexString(this.challengeLenghth));
	}
}
