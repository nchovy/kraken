package org.krakenapps.rpc.impl;

import org.krakenapps.confdb.CollectionName;
import org.krakenapps.rpc.RpcPeer;
import org.krakenapps.rpc.RpcTrustLevel;

@CollectionName("peers")
public class RpcPeerConfig implements RpcPeer {
	private String guid;
	private String password; // hash
	private RpcTrustLevel trustLevel;

	public RpcPeerConfig() {
	}

	public RpcPeerConfig(String guid, String password, int trustLevel) {
		this.guid = guid;
		this.password = password;
		this.trustLevel = RpcTrustLevel.parse(trustLevel);
	}

	@Override
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public RpcTrustLevel getTrustLevel() {
		return trustLevel;
	}

	public void setTrustLevel(RpcTrustLevel trustLevel) {
		this.trustLevel = trustLevel;
	}

}
