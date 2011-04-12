package org.krakenapps.pcap.decoder.smb;

public interface TransData {
	public static final short SMB_FIND_CLOSE_AFTER_REQUEST = 0x0001;
	public static final short SMB_FIND_CLOSE_AT_EOS = 0x0002;
	public static final short SMB_FIND_RETURN_RESUME_KEYS = 0x0004;
	public static final short SMB_FIND_CONTINUE_FROM_LAST = 0x0008;
	public static final short SMB_FIND_WITH_BACKUP_INTENT = 0x0010;
	public String toString();
}
