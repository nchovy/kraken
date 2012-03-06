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
package org.krakenapps.base.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.base.SentryProxy;
import org.krakenapps.base.SentryProxyEventListener;
import org.krakenapps.base.SentryGuidChecker;
import org.krakenapps.base.SentryProxyRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "sentry-proxy-registry")
@Provides
public class SentryProxyRegistryImpl implements SentryProxyRegistry {
	private final Logger logger = LoggerFactory.getLogger(SentryProxyRegistryImpl.class.getName());

	private ConcurrentMap<String, SentryProxy> sentryMap;
	private Set<SentryProxyEventListener> callbacks;
	private List<SentryGuidChecker> checkers = new ArrayList<SentryGuidChecker>();

	public SentryProxyRegistryImpl() {
		sentryMap = new ConcurrentHashMap<String, SentryProxy>();
		callbacks = Collections.newSetFromMap(new ConcurrentHashMap<SentryProxyEventListener, Boolean>());
	}

	@Override
	public Collection<String> getSentryGuids() {
		return Collections.unmodifiableCollection(sentryMap.keySet());
	}

	@Override
	public SentryProxy getSentry(String guid) {
		return sentryMap.get(guid);
	}

	@Override
	public void register(SentryProxy sentry) {
		if (sentry == null)
			throw new IllegalArgumentException("sentry must be not null");

		if (!checkGuid(sentry.getGuid()))
			throw new IllegalStateException("login not allowed: " + sentry.getGuid());

		SentryProxy old = sentryMap.putIfAbsent(sentry.getGuid(), sentry);
		if (old != null) {
			// TODO: connection is not removed clearly when network environment
			// is unstable. you should find and remove bug.
			logger.warn("kraken-base: disconnect duplicated old sentry [{}]", sentry.getGuid());
			try {
				old.close();
			} catch (Exception e) {
				logger.error("kraken-base: error while closing duplicated sentry", e);
			}

			// insert new sentry proxy
			sentryMap.put(sentry.getGuid(), sentry);
		}

		// invoke connect callbacks
		for (SentryProxyEventListener callback : callbacks) {
			try {
				callback.sentryConnected(sentry);
			} catch (Exception e) {
				logger.warn("kraken-base: sentry connect event should not throw any exception", e);
			}
		}
	}

	private boolean checkGuid(String guid) {
		synchronized (checkers) {
			for (SentryGuidChecker checker : checkers)
				if (!checker.check(guid))
					return false;
		}
		return true;
	}

	@Override
	public void unregister(SentryProxy sentry) {
		if (sentry == null)
			throw new IllegalArgumentException("sentry must be not null");

		if (!sentryMap.containsKey(sentry.getGuid()))
			return;

		// invoke disconnect callbacks
		for (SentryProxyEventListener callback : callbacks) {
			try {
				callback.sentryDisconnected(sentry);
			} catch (Exception e) {
				logger.warn("kraken-base: sentry disconnect event should not throw any exception", e);
			}
		}

		// remove
		sentryMap.remove(sentry.getGuid());
	}

	@Override
	public void addListener(SentryProxyEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("callback must be not null");

		callbacks.add(callback);
	}

	@Override
	public void removeListener(SentryProxyEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("callback must be not null");

		callbacks.remove(callback);
	}

	@Override
	public void addGuidChecker(SentryGuidChecker checker) {
		if (checker == null)
			throw new IllegalArgumentException("checker must be not null");

		synchronized (checkers) {
			checkers.add(checker);
		}
	}

	@Override
	public void removeGuidChecker(SentryGuidChecker checker) {
		if (checker == null)
			throw new IllegalArgumentException("checker must be not null");

		synchronized (checkers) {
			checkers.remove(checker);
		}
	}
}
