/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.ftp.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.ftp.FtpConnectProfile;
import org.krakenapps.ftp.FtpProfileService;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;

@Component(name = "ftp-profile-service")
@Provides
public class FtpProfileServiceImpl implements FtpProfileService {
	@Requires
	private PreferencesService prefsvc;

	@Override
	public void createProfile(FtpConnectProfile profile) {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			if (root.nodeExists(profile.getName()))
				throw new IllegalStateException("duplicated profile name");

			Preferences p = root.node(profile.getName());
			p.put("host", profile.getHost());
			p.put("port", String.valueOf(profile.getPort()));
			p.put("account", profile.getAccount());
			p.put("password", profile.getPassword());

			p.flush();
			p.sync();
		} catch (BackingStoreException e) {
			throw new IllegalStateException("io error", e);
		}
	}

	@Override
	public void removeProfile(String name) {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			if (!root.nodeExists(name))
				throw new IllegalStateException("profile not found");

			root.node(name).removeNode();

			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			throw new IllegalStateException("io error", e);
		}
	}

	@Override
	public Collection<FtpConnectProfile> getProfiles() {
		List<FtpConnectProfile> profiles = new ArrayList<FtpConnectProfile>();

		try {
			Preferences root = prefsvc.getSystemPreferences();
			for (String name : root.childrenNames()) {
				Preferences p = root.node(name);
				String host = p.get("host", null);
				int port = Integer.parseInt(p.get("port", "21"));
				String account = p.get("account", null);
				String password = p.get("password", null);

				profiles.add(new FtpConnectProfile(name, host, port, account, password));
			}
		} catch (BackingStoreException e) {
			throw new IllegalStateException("io error", e);
		}

		return profiles;
	}

	@Override
	public FtpConnectProfile getProfile(String name) {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			if (!root.nodeExists(name))
				return null;
			Preferences p = root.node(name);
			String host = p.get("host", null);
			int port = Integer.parseInt(p.get("port", "21"));
			String account = p.get("account", null);
			String password = p.get("password", null);

			return new FtpConnectProfile(name, host, port, account, password);
		} catch (BackingStoreException e) {
			throw new IllegalStateException("io error", e);
		}
	}

}
