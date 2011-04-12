package org.krakenapps.rpc;

import java.util.Collection;

public interface RpcPeerRegistry {
	Collection<String> getPeerGuids();

	void authenticate(String guid, String nonce, String hash);
	
	String calculatePasswordHash(String password);

	String calculatePasswordHash(String password, String nonce);

	RpcPeer findPeer(String guid);

	void register(RpcPeer peer);

	void unregister(String guid);
}
