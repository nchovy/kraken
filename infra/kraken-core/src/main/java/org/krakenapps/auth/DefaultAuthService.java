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
 package org.krakenapps.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.auth.api.AuthCallback;
import org.krakenapps.auth.api.AuthProfile;
import org.krakenapps.auth.api.AuthProvider;
import org.krakenapps.auth.api.AuthService;
import org.krakenapps.auth.api.AuthStrategy;
import org.krakenapps.auth.api.EmptyAuthCallback;
import org.krakenapps.auth.api.UserCredentials;
import org.krakenapps.auth.api.UserPrincipal;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class DefaultAuthService implements AuthService {
	private BundleContext bc;
	private ConfigService conf;
	private ConcurrentMap<String, AuthProvider> providers;
	private ConcurrentMap<String, AuthProfile> profiles;
	private AuthProviderTracker tracker;

	public DefaultAuthService(BundleContext bc, ConfigService conf) {
		this.bc = bc;
		this.conf = conf;
		this.providers = new ConcurrentHashMap<String, AuthProvider>();
		this.tracker = new AuthProviderTracker();

		// add providers and start tracker
		try {
			ServiceReference[] refs = bc.getAllServiceReferences(AuthProvider.class.getName(), null);
			if (refs != null) {
				for (ServiceReference ref : refs) {
					AuthProvider p = (AuthProvider) bc.getService(ref);
					providers.put(p.getName(), p);
				}
			}
		} catch (InvalidSyntaxException e) {
		}

		// read all profiles
		ConfigDatabase db = conf.ensureDatabase("kraken-core");
		ConfigCollection col = db.ensureCollection("auth-profile");
		ConfigIterator it = col.findAll();

		while (it.hasNext()) {
			Config c = it.next();
			AuthProfile p = c.getDocument(AuthProfile.class);
			profiles.put(p.getName(), p);
		}
		
		tracker.open();
	}

	@Override
	public Collection<AuthProvider> getProviders() {
		return providers.values();
	}

	@Override
	public AuthProvider getProvider(String name) {
		return providers.get(name);
	}

	@Override
	public Collection<AuthProfile> getProfiles() {
		return profiles.values();
	}

	@Override
	public AuthProfile getProfile(String name) {
		return profiles.get(name);
	}

	@Override
	public void createProfile(AuthProfile p) {
		AuthProfile old = profiles.putIfAbsent(p.getName(), p);
		if (old != null)
			throw new IllegalStateException("duplicated profile name: " + p.getName());

		ConfigDatabase db = conf.getDatabase("kraken-core");
		ConfigCollection col = db.ensureCollection("auth-profile");
		col.add(PrimitiveConverter.serialize(p), "kraken-core", "created auth profile: " + p.getName());
	}

	@Override
	public void updateProfile(AuthProfile p) {
		if (!profiles.containsKey(p.getName()))
			throw new IllegalStateException("profile not found: " + p.getName());

		profiles.put(p.getName(), p);

		ConfigDatabase db = conf.getDatabase("kraken-core");
		ConfigCollection col = db.ensureCollection("auth-profile");
		Config c = col.findOne(Predicates.field("name", p.getName()));
		if (c != null) {
			c.setDocument(PrimitiveConverter.serialize(p));
			col.update(c, false, "kraken-core", "created auth profile: " + p.getName());
		}
	}

	@Override
	public void removeProfile(String name) {
		ConfigDatabase db = conf.getDatabase("kraken-core");
		ConfigCollection col = db.ensureCollection("auth-profile");
		Config c = col.findOne(Predicates.field("name", name));
		if (c != null)
			col.remove(c, false, "kraken-core", "removed auth profile: " + name);
	}

	@Override
	public void authenticate(String profile, UserPrincipal principal, UserCredentials credentials, AuthCallback callback) {
		AuthProfile p = profiles.get(profile);
		if (p == null)
			throw new IllegalStateException("profile not found: " + profile);

		ChainedAuthCallback chained = new ChainedAuthCallback(principal, credentials, p, callback);
		chained.check();
	}

	private class ChainedAuthCallback extends EmptyAuthCallback {
		private UserPrincipal principal;
		private UserCredentials credentials;
		private AuthProfile profile;
		private List<String> providerNames;
		private AuthCallback userCallback;

		public ChainedAuthCallback(UserPrincipal principal, UserCredentials credentials, AuthProfile profile,
				AuthCallback userCallback) {
			this.principal = principal;
			this.credentials = credentials;
			this.profile = profile;
			this.providerNames = new ArrayList<String>(profile.getProviders());
			this.userCallback = userCallback;
		}

		public void check() {
			String providerName = providerNames.remove(0);
			AuthProvider provider = providers.get(providerName);
			if (provider == null)
				throw new IllegalStateException("provider not found: " + providerName);

			provider.authenticate(principal, credentials, this);
		}

		@Override
		public void onSuccess(AuthProvider provider, UserPrincipal principal, UserCredentials credentials) {
			userCallback.onSuccess(provider, principal, credentials);

			if (profile.getStrategy() == AuthStrategy.MatchAny)
				userCallback.onFail(profile, principal, credentials);
			else if (providerNames.size() == 0)
				userCallback.onSuccess(profile, principal, credentials);
			else
				check();
		}

		@Override
		public void onFail(AuthProvider provider, UserPrincipal principal, UserCredentials credentials) {
			userCallback.onFail(provider, principal, credentials);

			if (profile.getStrategy() == AuthStrategy.MatchAny)
				check();
			else
				userCallback.onFail(profile, principal, credentials);
		}
	}

	private class AuthProviderTracker extends ServiceTracker {

		public AuthProviderTracker() {
			super(bc, AuthProvider.class.getName(), null);
		}

		@Override
		public Object addingService(ServiceReference reference) {
			AuthProvider p = (AuthProvider) bc.getService(reference);
			providers.put(p.getName(), p);
			return p;
		}

		@Override
		public void removedService(ServiceReference reference, Object service) {
			AuthProvider p = (AuthProvider) service;
			providers.remove(p);
		}
	}
}
