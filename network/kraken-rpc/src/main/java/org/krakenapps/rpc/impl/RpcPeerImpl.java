package org.krakenapps.rpc.impl;

import org.krakenapps.rpc.RpcPeer;
import org.krakenapps.rpc.RpcTrustLevel;

public class RpcPeerImpl implements RpcPeer {
	private String guid;
	private String password; // hash
	private RpcTrustLevel trustLevel;

	public RpcPeerImpl(String guid, String password, int trustLevel) {
		this.guid = guid;
		this.password = password;
		this.trustLevel = RpcTrustLevel.parse(trustLevel);
	}

	@Override
	public String getGuid() {
		return guid;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public RpcTrustLevel getTrustLevel() {
		return trustLevel;
	}

}
