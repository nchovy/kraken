/*
 * Copyright 2011 Future Systems, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
