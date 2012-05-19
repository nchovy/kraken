package org.krakenapps.rpc;

public interface RpcPeer {
	String getGuid();

	String getPassword();

	RpcTrustLevel getTrustLevel();
}
