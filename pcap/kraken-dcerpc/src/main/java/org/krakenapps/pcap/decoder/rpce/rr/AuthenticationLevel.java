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

public enum AuthenticationLevel {

	RPC_C_AUTHN_LEVEL_DEFAULT(0),
	RPC_C_AUTHN_LEVEL_NONE(1),
	RPC_C_AUTHN_LEVEL_CONNECT(2),
	RPC_C_AUTHN_LEVEL_CALL(3),
	RPC_C_AUTHN_LEVEL_PKT(4),
	RPC_C_AUTHN_LEVEL_PKT_INTEGRITY(5),
	RPC_C_AUTHN_LEVEL_PKT_PRIVACY(6);
	AuthenticationLevel(int level){
		this.level = level;
	}
	private int level;
	static Map<Integer, AuthenticationLevel> levelMap = new HashMap<Integer, AuthenticationLevel>();
	static{
		for(AuthenticationLevel level : AuthenticationLevel.values()){
			levelMap.put(level.getLevel(), level);
		}
	}
	public int getLevel(){
		return level;
	}
	public static AuthenticationLevel parse(int level){
		return levelMap.get(level);
	}
}
