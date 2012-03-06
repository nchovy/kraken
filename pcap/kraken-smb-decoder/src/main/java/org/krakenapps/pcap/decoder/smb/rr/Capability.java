package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;

public enum Capability {
	CAP_RAW_MODE(0x00000001),
	CAP_MPX_MODE(0x00000002),
	CAP_UNICODE(0x00000004),
	CAP_LARGE_FILES(0x00000008),
	CAP_NT_SMBS(0x00000010),
	CAP_RPC_REMOTE_APIS(0x00000020),
	CAP_STATUS32(0x00000040),
	CAP_LEVEL_II_OPLOCKS(0x00000080),
	CAP_LOCK_AND_READ(0x00000100),
	CAP_NT_FIND(0x00000200),
	CAP_DFS(0x00001000),
	CAP_INFOLEVEL_PASSTHRU(0x00002000),
	CAP_LARGE_READX(0x00004000),
	CAP_LARGE_WRITEX(0x00008000),
	CAP_LWIO(0x00010000),
	CAP_UNIX(0x00800000),
	CAP_COMPRESSED_DATA(0x02000000),
	CAP_DYNAMIC_REAUTH(0x20000000),
	CAP_PERSISTENT_HANDLES(0x40000000),
	CAP_EXTENDED_SECURITY(0x80000000);
	Capability(int cap){
		this.cap = cap;
	}
	private int cap;
	private static Map<Integer , Capability> capabilityMap = new HashMap<Integer, Capability>();
	static {
		for(Capability cap : Capability.values())
		{
			capabilityMap.put(cap.getCapability() ,cap );
		}
	}
	public int getCapability()
	{
		return cap;
	}
	public static Capability parse(int cap)
	{
		return capabilityMap.get(cap);
	}
}
