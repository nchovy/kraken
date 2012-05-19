package org.krakenapps.rpc.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.rpc.RpcPeer;
import org.krakenapps.rpc.RpcPeerRegistry;

public class RpcPeerRegistryImpl implements RpcPeerRegistry {
	private ConfigService conf;

	public RpcPeerRegistryImpl(ConfigService conf) {
		this.conf = conf;
	}

	@Override
	public Collection<String> getPeerGuids() {
		ConfigDatabase db = conf.ensureDatabase("kraken-rpc");
		List<String> peerGuids = new ArrayList<String>();
		for (RpcPeerConfig c : db.findAll(RpcPeerConfig.class).getDocuments(RpcPeerConfig.class)) {
			peerGuids.add(c.getGuid());
		}
		return peerGuids;
	}

	@Override
	public RpcPeer authenticate(String guid, String nonce, String hash) {
		RpcPeer peer = findPeer(guid);
		if (peer == null)
			throw new IllegalStateException("peer not found: " + guid);

		String result = PasswordUtil.calculatePasswordHash(peer.getPassword(), nonce);
		if (result.equals(hash))
			return peer;

		throw new IllegalStateException("password does not match: " + guid);
	}

	@Override
	public RpcPeer findPeer(String guid) {
		ConfigDatabase db = conf.ensureDatabase("kraken-rpc");
		Config c = db.findOne(RpcPeerConfig.class, Predicates.field("guid", guid));
		if (c == null)
			return null;

		return c.getDocument(RpcPeerConfig.class);
	}

	@Override
	public void register(RpcPeer peer) {
		ConfigDatabase db = conf.ensureDatabase("kraken-rpc");
		Config c = db.findOne(RpcPeerConfig.class, Predicates.field("guid", peer.getGuid()));

		if (c != null)
			throw new IllegalStateException("peer already exists: " + peer.getGuid());

		RpcPeerConfig config = new RpcPeerConfig();
		config.setGuid(peer.getGuid());
		config.setPassword(peer.getPassword());
		config.setTrustLevel(peer.getTrustLevel());
		db.add(config);
	}

	@Override
	public void unregister(String guid) {
		ConfigDatabase db = conf.ensureDatabase("kraken-rpc");
		Config c = db.findOne(RpcPeerConfig.class, Predicates.field("guid", guid));
		if (c == null)
			return;

		db.remove(c);
	}
}
