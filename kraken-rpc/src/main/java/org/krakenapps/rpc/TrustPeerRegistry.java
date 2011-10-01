package org.krakenapps.rpc;

import java.util.ArrayList;
import java.util.Collection;

import org.krakenapps.rpc.impl.RpcPeerImpl;
import org.krakenapps.rpc.impl.RpcTrustLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrustPeerRegistry implements RpcPeerRegistry {
	private final Logger logger = LoggerFactory.getLogger(TrustPeerRegistry.class.getName());

	@Override
	public Collection<String> getPeerGuids() {
		return new ArrayList<String>();
	}

	@Override
	public RpcPeer authenticate(String guid, String nonce, String hash) {
		// trust all password hash
		logger.trace("kraken-rpc: bypass auth for [guid={}, nonce={}, hash={}]", new Object[] { guid, nonce, hash });
		return new RpcPeerImpl(guid, null, RpcTrustLevel.High.getCode());
	}

	@Override
	public RpcPeer findPeer(String guid) {
		return null;
	}

	@Override
	public void register(RpcPeer peer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void unregister(String guid) {
		throw new UnsupportedOperationException();
	}
}
