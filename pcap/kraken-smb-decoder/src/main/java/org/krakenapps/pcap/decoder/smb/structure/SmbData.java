package org.krakenapps.pcap.decoder.smb.structure;

public interface SmbData {
	public static final short SMB_OPEN_QUERY_INFORMATION =0x0001;
	public static final short SMB_OPEN_OPLOCK = 0x0002;
	public static final short SMB_OPNE_OPBATCH = 0x0003;
	public static final short SMB_OPEN_EXTENDED_RESPONSE = 0x0010;
	public static final short RESERVED = (short) 0xFFE8;
	public boolean isMalformed();
	public void setMalformed(boolean malformed);
	public String toString();
}
