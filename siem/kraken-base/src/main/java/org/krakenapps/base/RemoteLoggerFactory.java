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
package org.krakenapps.base;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerFactoryEventListener;
import org.osgi.framework.BundleContext;

public class RemoteLoggerFactory implements LoggerFactory {
	private String namespace;
	private String fullName;
	private Set<LoggerFactoryEventListener> callbacks;

	private SentryProxy proxy;
	private RemoteLoggerFactoryInfo factoryInfo;

	public RemoteLoggerFactory(SentryProxy proxy, RemoteLoggerFactoryInfo factoryInfo) {
		this.namespace = proxy.getGuid();
		callbacks = Collections.newSetFromMap(new ConcurrentHashMap<LoggerFactoryEventListener, Boolean>());
		this.proxy = proxy;
		this.factoryInfo = factoryInfo;
	}

	@Override
	public void onStart(BundleContext bc) {
	}

	@Override
	public void onStop() {
	}

	@Override
	public String getName() {
		return factoryInfo.getName();
	}

	@Override
	public String getFullName() {
		if (fullName == null)
			fullName = namespace + "\\" + getName();

		return fullName;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public Logger newLogger(String name, String description, Properties config) {
		return newLogger("local", name, description, config);
	}

	@Override
	public Logger newLogger(String namespace, String name, String description, Properties config) {
		return newLogger(namespace, name, description, 0, null, config);
	}

	@Override
	public Logger newLogger(String namespace, String name, String description, long logCount, Date lastLogDate, Properties config) {
		try {
			return proxy.createRemoteLogger(factoryInfo.getName(), name, description, config);
		} catch (Exception e) {
			String msg = String.format("cannot create remote logger: proxy=%s, factory=%s, logger name=%s", proxy.getGuid(),
					factoryInfo.getName(), name);
			throw new IllegalStateException(msg, e);
		}
	}

	@Override
	public void deleteLogger(String name) {
		deleteLogger("local", name);
	}

	@Override
	public void deleteLogger(String namespace, String name) {
		try {
			proxy.removeRemoteLogger(name);
		} catch (Exception e) {
			String msg = String.format("cannot delete remote logger: proxy=%s, factory=%s, logger name=%s", proxy.getGuid(),
					factoryInfo.getName(), name);
			throw new IllegalStateException(msg, e);
		}
	}

	@Override
	public void addListener(LoggerFactoryEventListener callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeListener(LoggerFactoryEventListener callback) {
		callbacks.remove(callback);
	}

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		return factoryInfo.getConfigOptions();
	}

	@Override
	public Collection<Locale> getDescriptionLocales() {
		return factoryInfo.getDescriptionLocales();
	}

	@Override
	public Collection<Locale> getDisplayNameLocales() {
		return factoryInfo.getDisplayNameLocales();
	}

	@Override
	public String getDescription(Locale locale) {
		return factoryInfo.getDescription(locale);
	}

	@Override
	public String getDisplayName(Locale locale) {
		return factoryInfo.getDisplayName(locale);
	}

	@Override
	public String toString() {
		return String.format("fullname=%s, type=%s, description=%s", getFullName(), getDisplayName(Locale.ENGLISH),
				getDescription(Locale.ENGLISH));
	}
}
