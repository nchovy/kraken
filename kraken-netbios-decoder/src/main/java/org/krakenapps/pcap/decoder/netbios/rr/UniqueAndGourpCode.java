package org.krakenapps.pcap.decoder.netbios.rr;

import java.util.HashMap;
import java.util.Map;

public enum UniqueAndGourpCode {
	//Unique netBios Name
	UniqueNameServerService(0x00),
	UniqueNameGenericMachineName(0x03),
	UniqueNameLanManserverService(0x20),
	UniqueNameDomainMasterBrowser(0x1b),
	//Group Names
	GroupNameGenericName(0x03),
	GroupNameDomainControllerServers(0x1c),
	GroupNameLocalMasterBrowser(0x1d),
	GroupNameBrowserElectionService(0x1e);
	private int code;
	private UniqueAndGourpCode(int code) {
		this.code = code;
	}
	private static Map<Integer, UniqueAndGourpCode> codeMap = new HashMap<Integer, UniqueAndGourpCode>();
	public int getCode()
	{
		return this.code;
	}
	static{
		for(UniqueAndGourpCode code : UniqueAndGourpCode.values()){
			codeMap.put(code.getCode(), code);
		}
	}
	public UniqueAndGourpCode parser(int code)
	{
		return codeMap.get(code);
	}
}
