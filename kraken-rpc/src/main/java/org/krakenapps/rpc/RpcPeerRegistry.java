package org.krakenapps.rpc;

import java.util.Collection;

public interface RpcPeerRegistry {
	Collection<String> getPeerGuids();

	RpcPeer authenticate(String guid, String nonce, String hash);

	RpcPeer findPeer(String guid);

	void register(RpcPeer peer);

	void unregister(String guid);
}
