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
package org.krakenapps.syslogmon;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.log.api.AbstractLoggerFactory;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.log.api.LoggerSpecification;
import org.krakenapps.log.api.StringConfigType;
import org.krakenapps.syslog.Syslog;
import org.krakenapps.syslog.SyslogListener;
import org.krakenapps.syslog.SyslogServerRegistry;
import org.osgi.framework.BundleContext;

@Component(name = "syslog-logger-factory")
@Provides
public class SyslogLoggerFactory extends AbstractLoggerFactory implements SyslogListener {

	/**
	 * remote ip to logger mappings
	 */
	private ConcurrentMap<InetAddress, SyslogLogger> loggerMappings = new ConcurrentHashMap<InetAddress, SyslogLogger>();
	private List<LoggerConfigOption> configs;

	@Requires
	private SyslogServerRegistry syslogRegistry;

	@Override
	public void onStart(BundleContext bc) {
		super.onStart(bc);

		prepareConfigOptions();
		syslogRegistry.addSyslogListener(this);
	}

	@Override
	public void onStop() {
		loggerMappings.clear();

		if (syslogRegistry != null)
			syslogRegistry.removeSyslogListener(this);

		super.onStop();
	}

	private void prepareConfigOptions() {
		configs = new ArrayList<LoggerConfigOption>();
		{
			Map<Locale, String> names = new HashMap<Locale, String>();
			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			names.put(Locale.ENGLISH, "remote ip");
			descriptions.put(Locale.ENGLISH, "remote syslog sender's ip address");
			configs.add(new StringConfigType("remote_ip", names, descriptions, true));
		}
		{
			Map<Locale, String> names = new HashMap<Locale, String>();
			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			names.put(Locale.ENGLISH, "syslog facility");
			descriptions.put(Locale.ENGLISH, "syslog facility number");
			configs.add(new StringConfigType("facility", names, descriptions, true));
		}
	}

	@Override
	public String getName() {
		return "syslog";
	}

	@Override
	public String getDisplayName(Locale locale) {
		return "syslog logger";
	}

	@Override
	public String getDescription(Locale locale) {
		return "syslog logger";
	}

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		return configs;
	}

	@Override
	protected Logger createLogger(LoggerSpecification spec) {
		try {
			InetAddress remote = InetAddress.getByName(spec.getConfig().getProperty("remote_ip"));
			SyslogLogger logger = new SyslogLogger(spec, this);
			logger.setPassive(true);
			String facility = spec.getConfig().getProperty("facility");
			if (facility == null)
				throw new IllegalArgumentException("syslog facility is required");

			logger.setFacilities(parseFacilities(facility));

			Logger old = loggerMappings.putIfAbsent(remote, logger);
			if (old != null)
				throw new IllegalStateException("same syslog mapping already exists for " + old);

			return logger;
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("unknown remote_ip", e);
		}
	}

	private Set<Integer> parseFacilities(String facility) {
		Set<Integer> facilities = new HashSet<Integer>();
		String[] tokens = facility.split(",");
		for (String token : tokens)
			facilities.add(Integer.valueOf(token.trim()));
		return facilities;
	}

	@Override
	public void deleteLogger(String namespace, String name) {
		InetAddress target = null;
		String targetName = namespace + "\\" + name;
		for (InetAddress remote : loggerMappings.keySet()) {
			Logger logger = loggerMappings.get(remote);
			if (logger.getFullName().equals(targetName))
				target = remote;
		}

		if (target != null)
			loggerMappings.remove(target);

		// do post clean job
		super.deleteLogger(namespace, name);
	}

	@Override
	public void onReceive(Syslog syslog) {
		SyslogLogger logger = loggerMappings.get(syslog.getRemoteAddress().getAddress());
		if (logger == null)
			return;

		logger.push(syslog);
	}
}
