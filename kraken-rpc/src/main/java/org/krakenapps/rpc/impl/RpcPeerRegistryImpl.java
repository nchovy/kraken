package org.krakenapps.rpc.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.krakenapps.rpc.RpcPeer;
import org.krakenapps.rpc.RpcPeerRegistry;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcPeerRegistryImpl implements RpcPeerRegistry {
	private Logger logger = LoggerFactory.getLogger(RpcPeerRegistryImpl.class.getName());
	private Preferences prefs;

	public RpcPeerRegistryImpl(Preferences prefs) {
		this.prefs = prefs;
	}

	@Override
	public Collection<String> getPeerGuids() {
		Preferences root = getPeerRoot();
		try {
			return Arrays.asList(root.childrenNames());
		} catch (BackingStoreException e) {
		}
		return new ArrayList<String>();
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
		Preferences root = getPeerRoot();
		try {
			if (!root.nodeExists(guid))
				return null;

			Preferences p = root.node(guid);

			String password = p.get("password", null);
			int trustLevel = p.getInt("trust-level", 1);

			return new RpcPeerImpl(guid, password, trustLevel);
		} catch (BackingStoreException e) {
			logger.warn("kraken-rpc: cannot find peer", e);
		}
		return null;
	}

	@Override
	public void register(RpcPeer peer) {
		Preferences root = getPeerRoot();
		try {
			String guid = peer.getGuid();
			if (root.nodeExists(guid))
				throw new IllegalStateException("peer already exists: " + guid);

			Preferences p = root.node(guid);
			p.put("password", peer.getPassword());
			p.putInt("trust-level", peer.getTrustLevel().getCode());

			p.flush();
			p.sync();

		} catch (BackingStoreException e) {
			logger.warn("kraken-rpc: cannot register peer", e);
		}
	}

	@Override
	public void unregister(String guid) {
		Preferences root = getPeerRoot();
		try {
			if (!root.nodeExists(guid))
				return;

			root.node(guid).removeNode();
			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			logger.warn("kraken-rpc: cannot unregister peer", e);
		}
	}

	private Preferences getPeerRoot() {
		return prefs.node("/kraken-rpc/peers");
	}
}
