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
package org.krakenapps.syslogmon.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.log.api.AbstractLoggerFactory;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.log.api.LoggerSpecification;
import org.krakenapps.log.api.StringConfigType;
import org.krakenapps.syslog.Syslog;
import org.krakenapps.syslog.SyslogListener;
import org.krakenapps.syslog.SyslogServerRegistry;
import org.krakenapps.syslogmon.SyslogClassifier;
import org.krakenapps.syslogmon.SyslogClassifierRegistry;

@Component(name = "syslog-relay-logger-factory")
@Provides
public class SyslogRelayLoggerFactory extends AbstractLoggerFactory implements SyslogListener {
	private org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(SyslogRelayLoggerFactory.class.getName());

	@Requires
	private SyslogClassifierRegistry classifierRegistry;

	@Requires
	private SyslogServerRegistry syslogRegistry;

	private ConcurrentMap<InetAddress, String> classifierMappings;

	private ConcurrentMap<VirtualLoggerKey, SyslogRelayLogger> loggerMappings;

	private List<LoggerConfigOption> configOptions;

	@Validate
	public void start() {
		classifierMappings = new ConcurrentHashMap<InetAddress, String>();
		loggerMappings = new ConcurrentHashMap<SyslogRelayLoggerFactory.VirtualLoggerKey, SyslogRelayLogger>();
		prepareConfigOptions();
		syslogRegistry.addSyslogListener(this);
	}

	@Invalidate
	public void stop() {
		if (syslogRegistry != null)
			syslogRegistry.removeSyslogListener(this);
	}

	@Override
	public String getName() {
		return "syslog-relay";
	}

	@Override
	public String getDescription(Locale locale) {
		return "relayed syslog logger";
	}

	@Override
	public String getDisplayName(Locale locale) {
		return "syslogger for relayed syslog";
	}

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		return configOptions;
	}

	@Override
	protected Logger createLogger(LoggerSpecification spec) {
		SyslogRelayLogger virtualLogger = new SyslogRelayLogger(spec, this);
		try {
			Properties config = spec.getConfig();
			InetAddress remote = InetAddress.getByName(config.getProperty("remote_ip"));
			String classifier = config.getProperty("classifier");
			if (classifier == null)
				throw new IllegalArgumentException("classifier name should not be null");

			String identifier = config.getProperty("identifier");
			if (identifier == null)
				throw new IllegalArgumentException("identifier should not be null");

			// create mapping. classifier should be only one per remote address
			String oldClassifier = classifierMappings.putIfAbsent(remote, classifier);
			if (oldClassifier != null && !oldClassifier.equals(classifier))
				throw new IllegalArgumentException("classifier should be " + oldClassifier + " for " + remote);

			// set logger mapping
			loggerMappings.put(new VirtualLoggerKey(remote, identifier), virtualLogger);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("unknown remote_ip", e);
		}

		return virtualLogger;
	}

	private void prepareConfigOptions() {
		configOptions = new ArrayList<LoggerConfigOption>();
		{
			Map<Locale, String> names = new HashMap<Locale, String>();
			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			names.put(Locale.ENGLISH, "remote ip");
			descriptions.put(Locale.ENGLISH, "remote syslog sender's ip address");
			configOptions.add(new StringConfigType("remote_ip", names, descriptions, true));
		}
		{
			Map<Locale, String> names = new HashMap<Locale, String>();
			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			names.put(Locale.ENGLISH, "syslog classifier");
			descriptions.put(Locale.ENGLISH, "syslog classifier name");
			configOptions.add(new StringConfigType("classifier", names, descriptions, true));
		}
		{
			Map<Locale, String> names = new HashMap<Locale, String>();
			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			names.put(Locale.ENGLISH, "identifier");
			descriptions.put(Locale.ENGLISH, "logger identifier");
			configOptions.add(new StringConfigType("identifier", names, descriptions, true));
		}

	}

	@Override
	public void onReceive(Syslog syslog) {
		InetAddress remoteAddr = syslog.getRemoteAddress().getAddress();
		String classifierName = classifierMappings.get(remoteAddr);

		if (classifierName == null)
			return;

		SyslogClassifier classifier = classifierRegistry.getClassifier(classifierName);
		if (classifier == null) {
			slog.warn("kraken syslogmon: classifier not found, ip [{}], classifier [{}]", remoteAddr, classifierName);
			return;
		}

		String identifier = classifier.classify(syslog);
		if (identifier == null) {
			if (slog.isDebugEnabled())
				slog.debug("kraken syslogmon: identifier not found, ip [{}], syslog [{}]", remoteAddr, syslog);
			return;
		}

		VirtualLoggerKey key = new VirtualLoggerKey(remoteAddr, identifier);
		SyslogRelayLogger logger = loggerMappings.get(key);
		if (logger == null)
			return;

		logger.push(syslog);
	}

	private static class VirtualLoggerKey {
		public InetAddress ip;
		public String identifier;

		public VirtualLoggerKey(InetAddress ip, String identifier) {
			this.ip = ip;
			this.identifier = identifier;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
			result = prime * result + ((ip == null) ? 0 : ip.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			VirtualLoggerKey other = (VirtualLoggerKey) obj;
			if (identifier == null) {
				if (other.identifier != null)
					return false;
			} else if (!identifier.equals(other.identifier))
				return false;
			if (ip == null) {
				if (other.ip != null)
					return false;
			} else if (!ip.equals(other.ip))
				return false;
			return true;
		}
	}
}
