package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;

public enum HeaderFlagMask {

	SMB_FLAGS_LOCK_AND_READ_OK(0x01),
	SMB_FLAGS_BUF_AVAIL(0x02),
	Reserved(0x04),
	SMB_FLAGS_CASE_INSENSITIVE(0x08),
	SMB_FLAGS_CANONICALIZED_PATHS(0x10),
	SMB_FLAGS_OPLOCK(0x20),
	SMB_FLAGS_OPBATCH(0x40),
	SMB_FLAGS_REPLY(0x80);
	private static Map<Integer , HeaderFlagMask> maskMap = new HashMap<Integer, HeaderFlagMask>();
	static {
		for(HeaderFlagMask mask : HeaderFlagMask.values())
		{
			maskMap.put(mask.getMask() , mask);
		}
	}
	public int getMask(){
		return mask;
	}
	HeaderFlagMask(int mask)
	{
		this.mask = mask;
	}
	public HeaderFlagMask parse(int mask)
	{
		return maskMap.get(mask);
	}
	private int mask; 
}
