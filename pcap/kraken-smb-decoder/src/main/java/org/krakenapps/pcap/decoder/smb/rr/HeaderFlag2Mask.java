package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;

public enum HeaderFlag2Mask {

	SMB_FLAGS2_LONG_NAMES(0x0001),
	SMB_FLAGS2_EAS(0x0002),
	SMB_FLAGS2_SMB_SECURITY_SIGNATURE(0x0004),
	SMB_FLAGS2_COMPRESSED(0x0008),
	SMB_FLAGS2_SMB_SECURITY_SIGNATURE_REQUIRED(0x0010),
	SMB_FLAGS2_IS_LONG_NAME(0x0040),
	SMB_FLAGS2_REPARSE_PATH(0x0400),
	SMB_FLAGS2_EXTENDED_SECURITY(0x0800),
	SMB_FLAGS2_DFS(0x1000),
	SMB_FLAGS2_PAGING_IO(0x2000),
	SMB_FLAGS2_NT_STATUS(0x4000),
	SMB_FLAGS2_UNICODE(0x8000);
	private static Map<Integer , HeaderFlag2Mask> maskMap = new HashMap<Integer, HeaderFlag2Mask>();
	static {
		for(HeaderFlag2Mask mask : HeaderFlag2Mask.values())
		{
			maskMap.put(mask.getMask() , mask);
		}
	}
	public int getMask()
	{
		return mask;
	}
	public HeaderFlag2Mask parse(int mask)
	{
		return maskMap.get(mask);
	}
	HeaderFlag2Mask(int mask)
	{
		this.mask = mask;
	}
	private int mask; 
	
}
