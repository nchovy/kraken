package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;

public class AuthValueLevelCall implements AuthValue {

	private byte subType;
	private byte checksumLength;
	private byte[] checksum;

	public byte getSubType() {
		return subType;
	}

	public void setSubType(byte subType) {
		this.subType = subType;
	}

	public byte getChecksumLength() {
		return checksumLength;
	}

	public void setChecksumLength(byte checksumLength) {
		this.checksumLength = checksumLength;
	}

	public byte[] getChecksum() {
		return checksum;
	}

	public void setChecksum(byte[] checksum) {
		this.checksum = checksum;
	}

	@Override
	public void parse(Buffer b) {
		subType = b.get();
		checksumLength = b.get();
		checksum = new byte[checksumLength];
		b.gets(checksum);
	}
}
