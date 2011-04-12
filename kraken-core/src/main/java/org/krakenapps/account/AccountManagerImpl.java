/*
 * Copyright 2010 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.account;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.krakenapps.api.AccountManager;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountManagerImpl implements AccountManager {
	private static final String SALT = "k";
	private final Logger logger = LoggerFactory.getLogger(AccountManagerImpl.class.getName());
	private Preferences prefs;

	public AccountManagerImpl(Preferences prefs) {
		this.prefs = prefs;
		createDefaultAccount();
	}

	private void createDefaultAccount() {
		try {
			createAccount("root", "kraken");
		} catch (Exception e) {
			// ignore if already exists
		}
	}

	@Override
	public Collection<String> getAccounts() {
		List<String> accounts = new ArrayList<String>();
		try {
			Preferences prefs = getAccountPreferences();
			for (String name : prefs.childrenNames()) {
				accounts.add(name);
			}
		} catch (BackingStoreException e) {
			logger.warn("core: get accounts failed", e);
		}

		return accounts;
	}

	@Override
	public void createAccount(String name, String password) {
		Preferences prefs = getAccountPreferences();
		try {
			if (prefs.nodeExists(name))
				throw new IllegalStateException("duplicated name");

			String hash = hashPassword(password);
			Preferences p = prefs.node(name);
			p.put("password", hash);

			prefs.flush();
			prefs.sync();
		} catch (BackingStoreException e) {
			logger.warn("core: create account failed", e);
		}
	}

	@Override
	public void removeAccount(String name) {
		if (name.equals("root"))
			throw new IllegalArgumentException("cannot remove root account");
		
		try {
			Preferences accounts = getAccountPreferences();
			if (!accounts.nodeExists(name))
				return;

			accounts.node(name).removeNode();
			accounts.flush();
			accounts.sync();
		} catch (BackingStoreException e) {
			logger.warn("core: remove account failed", e);
		}
	}

	@Override
	public void changePassword(String name, String currentPassword, String newPassword) {
		Preferences prefs = getAccountPreferences();
		try {
			if (!prefs.nodeExists(name))
				throw new IllegalStateException("account not found");

			Preferences p = prefs.node(name);
			String hash = p.get("password", null);
			String currentPasswordHash = hashPassword(currentPassword);
			String newPasswordHash = hashPassword(newPassword);

			if (hash == null || !currentPasswordHash.equals(hash))
				throw new IllegalStateException("invalid current password");

			p.put("password", newPasswordHash);
			p.flush();
			p.sync();
		} catch (BackingStoreException e) {
			logger.warn("core: change password failed", e);
		}
	}

	@Override
	public boolean verifyPassword(String name, String password) {
		if (password == null)
			return false;

		try {
			Preferences prefs = getAccountPreferences();
			if (!prefs.nodeExists(name))
				return false;

			Preferences p = prefs.node(name);
			String hash = p.get("password", null);
			if (hash == null)
				return false;

			String computed = hashPassword(password);
			if (computed == null)
				return false;

			if (computed.equals(hash))
				return true;

		} catch (BackingStoreException e) {
			logger.warn("core: verify password failed", e);
		}
		return false;
	}

	private static String hashPassword(String text) {
		try {
			text = SALT + text;
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] sha1hash = new byte[40];
			md.update(text.getBytes("iso-8859-1"), 0, text.length());
			sha1hash = md.digest();
			return convertToHex(sha1hash);
		} catch (Exception e) {
			return null;
		}
	}

	private static String convertToHex(byte[] data) {
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

	private Preferences getAccountPreferences() {
		return prefs.node("/account");
	}
}
