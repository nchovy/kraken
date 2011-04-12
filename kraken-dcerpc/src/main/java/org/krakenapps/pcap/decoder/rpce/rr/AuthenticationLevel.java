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
