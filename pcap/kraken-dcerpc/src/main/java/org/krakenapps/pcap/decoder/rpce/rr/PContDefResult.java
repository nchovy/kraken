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

public enum PContDefResult {

	ACCEPTANCE(0x00),
	USER_REJECTION(0x01),
	PROVIDER_REJECTION(0x02),
	NEGOTIATE_ACK(0x03);
	PContDefResult(int code) {
		this.code = code;
	}
	static Map<Integer, PContDefResult> codeMap = new HashMap<Integer, PContDefResult>();
	static{
		for(PContDefResult code : PContDefResult.values()){
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode(){
		return code;
	}
	public static PContDefResult parse(int code){
		return codeMap.get(code);
	}
	private int code;
}
