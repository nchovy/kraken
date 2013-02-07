/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.pcap.decoder.rpce.rr;

import java.util.HashMap;
import java.util.Map;

public enum SecurityProvider {
	// extension sepecify the following values
	RPC_C_AUTHN_NONE(0x00),
	RPC_C_AUTHN_GSS_NEGOTIATE(0x09),
	RPC_C_AUTHN_AUTHN_WINNT(0x0A),
	RPC_C_AUTHN_GSS_SCHANNEL(0x0E),
	RPC_C_AUTHN_KERBEROS(0x10),
	RPC_C_AUTHN_NETLOGON(0x44),
	RPC_C_AUTHN_DEFAULT(0xFF);
	SecurityProvider(int code){
		this.code = code;
	}
	static Map<Integer, SecurityProvider> providerMap = new HashMap<Integer, SecurityProvider>();
	static{
		for(SecurityProvider provider : SecurityProvider.values()){
			providerMap.put(provider.getProvider(), provider);
		}
	}
	public static SecurityProvider parse(int code){
		return providerMap.get(code);
	}
	public int getProvider(){
		return code;
	}
	private int code;
}

