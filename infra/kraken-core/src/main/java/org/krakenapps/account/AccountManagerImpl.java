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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.api.AccountManager;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.auth.api.AuthCallback;
import org.krakenapps.auth.api.AuthProvider;
import org.krakenapps.auth.api.UserCredentials;
import org.krakenapps.auth.api.UserPrincipal;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.Predicates;

public class AccountManagerImpl implements AccountManager, AuthProvider {
	private static final String SALT = "kraken";
	private ConfigService conf;

	public AccountManagerImpl(ConfigService conf) {
		this.conf = conf;
		createDefaultAccount();
	}

	@Override
	public String getName() {
		return "local";
	}

	private void createDefaultAccount() {
		try {
			createAccount("root", SALT);
		} catch (Exception e) {
			// ignore if already exists
		}
	}

	@Override
	public void authenticate(UserPrincipal principal, UserCredentials credentials, AuthCallback callback) {
		System.out.println(principal + ", " + credentials);
		String password = (String) credentials.get("password");
		boolean result = verifyPassword(principal.getName(), password);
		if (result)
			callback.onSuccess(this, principal, credentials);
		else
			callback.onFail(this, principal, credentials);
	}

	@Override
	public Collection<String> getAccounts() {
		ConfigDatabase db = conf.getDatabase("kraken-core");
		ConfigCollection principals = db.ensureCollection("principal");

		List<String> accounts = new ArrayList<String>();
		ConfigIterator it = principals.findAll();
		while (it.hasNext()) {
			Config c = it.next();
			UserPrincipal p = c.getDocument(UserPrincipal.class);
			accounts.add(p.getName());
		}

		return accounts;
	}

	@Override
	public void createAccount(String name, String password) {
		ConfigDatabase db = conf.getDatabase("kraken-core");
		ConfigCollection principals = db.ensureCollection("principal");
		ConfigCollection credentials = db.ensureCollection("credential");

		Config c = principals.findOne(Predicates.field("login_name", name));
		if (c != null)
			throw new IllegalStateException("duplicated principal name: " + name);

		ConfigTransaction xact = db.beginTransaction(5000);
		try {
			UserPrincipal principal = new UserPrincipal(name);
			principals.add(xact, PrimitiveConverter.serialize(principal));

			String hash = hashPassword(SALT, password);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("domain", principal.getDomain());
			m.put("login_name", principal.getName());
			m.put("salt", SALT);
			m.put("password", hash);
			credentials.add(xact, m);

			xact.commit("kraken-core", "created account: " + name);
		} catch (Throwable e) {
			xact.rollback();
		}
	}

	@Override
	public void removeAccount(String name) {
		if (name.equals("root"))
			throw new IllegalArgumentException("cannot remove root account");

		ConfigDatabase db = conf.getDatabase("kraken-core");
		ConfigCollection principals = db.ensureCollection("principal");
		ConfigCollection credentials = db.ensureCollection("credential");

		ConfigTransaction xact = db.beginTransaction(5000);
		try {
			Config c1 = principals.findOne(Predicates.field("login_name", name));
			if (c1 == null)
				return;

			db.remove(c1);

			Config c2 = credentials.findOne(Predicates.field("login_name", name));
			if (c2 == null)
				return;

			db.remove(c2);
			xact.commit("kraken-core", "removed account: " + name);
		} catch (Throwable e) {
			xact.rollback();
		}
	}

	@Override
	public void changePassword(String name, String currentPassword, String newPassword) {
		ConfigDatabase db = conf.getDatabase("kraken-core");
		ConfigCollection credentials = db.ensureCollection("credential");
		Config c = credentials.findOne(Predicates.field("login_name", name));
		if (c == null)
			throw new IllegalStateException("account not found");

		@SuppressWarnings("unchecked")
		Map<String, Object> m = (Map<String, Object>) c.getDocument();

		String salt = (String) m.get("salt");
		String hash = (String) m.get("password");
		String currentPasswordHash = hashPassword(salt, currentPassword);
		String newPasswordHash = hashPassword(salt, newPassword);

		if (hash == null || !currentPasswordHash.equals(hash))
			throw new IllegalStateException("invalid current password");

		m.put("password", newPasswordHash);
		c.setDocument(m);
		credentials.update(c, false, "kraken-core", "changed password for " + name);
	}

	@Override
	public boolean verifyPassword(String name, String password) {

		if (password == null)
			return false;

		ConfigDatabase db = conf.getDatabase("kraken-core");
		ConfigCollection credentials = db.ensureCollection("credential");
		Config c = credentials.findOne(Predicates.field("login_name", name));
		if (c == null)
			return false;

		@SuppressWarnings("unchecked")
		Map<String, Object> m = (Map<String, Object>) c.getDocument();

		String salt = (String) m.get("salt");
		String hash = (String) m.get("password");
		if (hash == null)
			return false;

		String computed = hashPassword(salt, password);
		if (computed == null)
			return false;

		if (computed.equals(hash))
			return true;

		return false;
	}

	private static String hashPassword(String salt, String text) {
		try {
			text = salt + text;
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
}
