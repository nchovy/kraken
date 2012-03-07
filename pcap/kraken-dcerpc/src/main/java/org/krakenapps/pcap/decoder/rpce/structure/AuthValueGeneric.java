package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class AuthValueGeneric implements AuthValue {

	private int assocUuidCrc;
	private byte subType;
	private byte checksumLength;
	private short credLength;
	private byte[] credentials;
	private byte[] checksum;

	public int getAssocUuidCrc() {
		return assocUuidCrc;
	}

	public void setAssocUuidCrc(int assocUuidCrc) {
		this.assocUuidCrc = assocUuidCrc;
	}

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

	public short getCredLength() {
		return credLength;
	}

	public void setCredLength(short credLength) {
		this.credLength = credLength;
	}

	public byte[] getCredentials() {
		return credentials;
	}

	public void setCredentials(byte[] credentials) {
		this.credentials = credentials;
	}

	public byte[] getChecksum() {
		return checksum;
	}

	public void setChecksum(byte[] checksum) {
		this.checksum = checksum;
	}

	@Override
	public void parse(Buffer b) {
		assocUuidCrc = ByteOrderConverter.swap(b.getInt());
		subType = b.get();
		checksumLength = b.get();
		credLength = ByteOrderConverter.swap(b.getShort());
		credentials = new byte[credLength];
		checksum = new byte[checksumLength];
		b.gets(credentials);
		b.gets(checksum);
	}

}
