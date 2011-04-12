package org.krakenapps.rpc.impl;

import java.security.MessageDigest;
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
	public void authenticate(String guid, String nonce, String hash) {
		RpcPeer peer = findPeer(guid);
		if (peer == null)
			throw new IllegalStateException("peer not found: " + guid);

		String result = calculatePasswordHash(peer.getPassword(), nonce);
		if (result.equals(hash))
			return;

		throw new IllegalStateException("password does not match: " + guid);
	}

	@Override
	public String calculatePasswordHash(String password) {
		return SHA1(password);
	}

	@Override
	public String calculatePasswordHash(String password, String nonce) {
		return SHA1(password + nonce);
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

	private String SHA1(String text) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] sha1hash = new byte[40];
			md.update(text.getBytes("iso-8859-1"), 0, text.length());
			sha1hash = md.digest();
			return convertToHex(sha1hash);
		} catch (Exception e) {
			logger.warn("kraken-rpc: SHA1 hash failed", e);
			return null;
		}
	}

	private String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}
}
