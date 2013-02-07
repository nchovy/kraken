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
package org.krakenapps.snmpmon;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.krakenapps.snmp.SnmpTrap;
import org.krakenapps.snmp.SnmpTrapReceiver;
import org.krakenapps.snmp.SnmpTrapService;

@Component(name = "snmptrap-logger-factory")
@Provides
public class SnmpTrapLoggerFactory extends AbstractLoggerFactory implements SnmpTrapReceiver {
	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SnmpTrapLoggerFactory.class.getName());

	@Requires
	private SnmpTrapService trapService;

	private ConcurrentMap<InetAddress, SnmpTrapLogger> trapMappings;

	private List<LoggerConfigOption> configs;

	public SnmpTrapLoggerFactory() {
		trapMappings = new ConcurrentHashMap<InetAddress, SnmpTrapLogger>();
		prepareConfigOptions();
	}

	@Validate
	public void start() {
		trapMappings.clear();
		trapService.addReceiver(this);
	}

	@Invalidate
	public void stop() {
		if (trapService != null)
			trapService.removeReceiver(this);
	}

	@Override
	public String getName() {
		return "snmptrap";
	}

	@Override
	public String getDescription(Locale locale) {
		return "snmp trap logger";
	}

	@Override
	public String getDisplayName(Locale locale) {
		return "snmp trap logger";
	}

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		return configs;
	}

	@Override
	protected Logger createLogger(LoggerSpecification spec) {
		try {
			InetAddress remote = InetAddress.getByName(spec.getConfig().getProperty("remote_ip"));
			SnmpTrapLogger logger = new SnmpTrapLogger(spec, this);
			Logger old = trapMappings.putIfAbsent(remote, logger);
			if (old != null)
				throw new IllegalStateException("same snmp trap logger already exists for " + old);

			return logger;
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("unknown remote_ip", e);
		}
	}

	@Override
	public void deleteLogger(String namespace, String name) {
		InetAddress target = null;
		String targetName = namespace + "\\" + name;
		for (InetAddress remote : trapMappings.keySet()) {
			Logger logger = trapMappings.get(remote);
			if (logger.getFullName().equals(targetName))
				target = remote;
		}

		if (target != null)
			trapMappings.remove(target);

		// do post clean job
		super.deleteLogger(namespace, name);
	}

	@Override
	public void handle(SnmpTrap trap) {
		SnmpTrapLogger trapLogger = trapMappings.get(trap.getRemoteAddress().getAddress());
		if (trapLogger != null)
			trapLogger.push(trap);
		else
			logger.trace("kraken snmpmon: discard trap [{}]", trap);
	}

	private void prepareConfigOptions() {
		configs = new ArrayList<LoggerConfigOption>();
		{
			Map<Locale, String> names = new HashMap<Locale, String>();
			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			names.put(Locale.ENGLISH, "remote ip");
			descriptions.put(Locale.ENGLISH, "remote snmp trap sender's ip address");
			configs.add(new StringConfigType("remote_ip", names, descriptions, true));
		}
	}

}
