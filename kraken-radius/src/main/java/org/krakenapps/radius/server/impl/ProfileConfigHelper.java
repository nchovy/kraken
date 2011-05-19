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
package org.krakenapps.radius.server.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.krakenapps.radius.server.RadiusProfile;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfileConfigHelper {
	private static final String USER_DATABASE_ROOT_KEY = "user_databases";
	private static final String AUTHENTICATOR_ROOT_KEY = "authenticators";
	private static final String PROFILE_ROOT_KEY = "profiles";

	private ProfileConfigHelper() {
	}

	public static List<RadiusProfile> loadProfiles(PreferencesService prefsvc) {
		List<RadiusProfile> profiles = new ArrayList<RadiusProfile>();

		try {
			Preferences root = prefsvc.getSystemPreferences().node(PROFILE_ROOT_KEY);
			for (String name : root.childrenNames()) {
				profiles.add(loadProfile(prefsvc, name));
			}
		} catch (BackingStoreException e) {
			Logger logger = LoggerFactory.getLogger(ProfileConfigHelper.class.getName());
			logger.error("kraken radius: cannot load profiles", e);
		}

		return profiles;
	}

	public static RadiusProfile loadProfile(PreferencesService prefsvc, String name) {
		try {
			RadiusProfile profile = new RadiusProfile();
			Preferences p = prefsvc.getSystemPreferences().node(PROFILE_ROOT_KEY).node(name);
			Preferences authNode = p.node(AUTHENTICATOR_ROOT_KEY);
			Preferences udbNode = p.node(USER_DATABASE_ROOT_KEY);

			// load authenticator names
			List<String> authNames = Arrays.asList(authNode.childrenNames());
			List<String> udbNames = Arrays.asList(udbNode.childrenNames());

			profile.setName(name);
			profile.setSharedSecret(p.get("shared_secret", null));
			profile.setAuthenticators(authNames);
			profile.setUserDatabases(udbNames);
			return profile;
		} catch (BackingStoreException e) {
			Logger logger = LoggerFactory.getLogger(ProfileConfigHelper.class.getName());
			logger.error("kraken radius: cannot load profiles", e);
			throw new RuntimeException(e);
		}
	}

	public static void createProfile(PreferencesService prefsvc, RadiusProfile profile) {
		if (profile.getName() == null)
			throw new IllegalArgumentException("profile name should be not null");

		try {
			Preferences root = prefsvc.getSystemPreferences().node(PROFILE_ROOT_KEY);
			Preferences p = root.node(profile.getName());
			p.put("shared_secret", profile.getSharedSecret());
			createSubNodes(profile, p);

			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			Logger logger = LoggerFactory.getLogger(ProfileConfigHelper.class.getName());
			logger.error("kraken radius: cannot save profile", e);
		}
	}

	public static void updateProfile(PreferencesService prefsvc, RadiusProfile profile) {
		if (profile.getName() == null)
			throw new IllegalArgumentException("profile name should be not null");

		try {
			Preferences root = prefsvc.getSystemPreferences().node(PROFILE_ROOT_KEY);
			Preferences p = root.node(profile.getName());

			p.put("shared_secret", profile.getSharedSecret());
			p.node(AUTHENTICATOR_ROOT_KEY).removeNode();
			p.node(USER_DATABASE_ROOT_KEY).removeNode();

			createSubNodes(profile, p);

			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			Logger logger = LoggerFactory.getLogger(ProfileConfigHelper.class.getName());
			logger.error("kraken radius: cannot save profile", e);
		}
	}

	private static void createSubNodes(RadiusProfile profile, Preferences p) {
		Preferences auth = p.node(AUTHENTICATOR_ROOT_KEY);
		for (String name : profile.getAuthenticators()) {
			auth.node(name);
		}

		Preferences udb = p.node(USER_DATABASE_ROOT_KEY);
		for (String name : profile.getUserDatabases()) {
			udb.node(name);
		}
	}

	public static void removeProfile(PreferencesService prefsvc, String profileName) {
		if (profileName == null)
			throw new IllegalArgumentException("profile name should be not null");

		try {
			Preferences root = prefsvc.getSystemPreferences().node(PROFILE_ROOT_KEY);
			Preferences p = root.node(profileName);
			p.removeNode();

			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			Logger logger = LoggerFactory.getLogger(ProfileConfigHelper.class.getName());
			logger.error("kraken radius: cannot purge profile", e);
		}
	}
}
