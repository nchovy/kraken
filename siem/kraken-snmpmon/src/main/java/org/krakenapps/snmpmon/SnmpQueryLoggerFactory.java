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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.log.api.AbstractLoggerFactory;
import org.krakenapps.log.api.IntegerConfigType;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.log.api.LoggerSpecification;
import org.krakenapps.log.api.StringConfigType;
import org.slf4j.LoggerFactory;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 * @author stania
 */
@Component(name = "snmpmon-query-logger-factory")
@Provides
public class SnmpQueryLoggerFactory extends AbstractLoggerFactory {
	private org.slf4j.Logger logger = LoggerFactory.getLogger(SnmpQueryLoggerFactory.class);
	private TransportMapping transport;
	private Snmp snmp;

	public SnmpQueryLoggerFactory() {
		try {
			this.transport = new DefaultUdpTransportMapping(new UdpAddress());
			transport.listen();
			this.snmp = new Snmp(transport);
		} catch (IOException e) {
			logger.error("kraken snmpmon: ", e);
		}
	}

	@Validate
	public void validate() {
		SnmpQueryLogger.open();
	}

	@Invalidate
	public void invalidate() {
		SnmpQueryLogger.close();
		try {
			if (transport != null)
				transport.close();
		} catch (IOException e) {
		}
		try {
			if (snmp != null)
				snmp.close();
		} catch (IOException e) {
		}
	}

	@Override
	public String getName() {
		return "snmpmon";
	}

	@Override
	public Collection<Locale> getDisplayNameLocales() {
		ArrayList<Locale> locales = new ArrayList<Locale>();
		locales.add(Locale.ENGLISH);
		return locales;
	}

	@Override
	public String getDisplayName(Locale locale) {
		return "network usage logger";
	}

	@Override
	public Collection<Locale> getDescriptionLocales() {
		ArrayList<Locale> locales = new ArrayList<Locale>();
		locales.add(Locale.ENGLISH);
		return locales;
	}

	@Override
	public String getDescription(Locale locale) {
		return "network usage logs using SNMP query";
	}

	public enum ConfigOption {
		AgentIP("agent_ip"), AgentPort("agent_port"), SnmpCommunity("snmp_community"), SnmpVersion("snmp_version");

		private String configKey;

		private ConfigOption(String configKey) {
			this.configKey = configKey;
		}

		public String getConfigKey() {
			return configKey;
		}
	}

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		List<LoggerConfigOption> types = new ArrayList<LoggerConfigOption>();

		{
			Map<Locale, String> displayNames = new HashMap<Locale, String>();
			displayNames.put(Locale.ENGLISH, "Agent IP");
			displayNames.put(Locale.KOREAN, "대상 에이전트 IP 주소");

			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			descriptions.put(Locale.ENGLISH, "Address of target network agent");
			descriptions.put(Locale.KOREAN, "대상 네트워크 장비의 주소");

			types.add(new StringConfigType(ConfigOption.AgentIP.configKey, displayNames, descriptions, true));
		}
		{
			Map<Locale, String> displayNames = new HashMap<Locale, String>();
			displayNames.put(Locale.ENGLISH, "Agent SNMP Port");
			displayNames.put(Locale.KOREAN, "대상 에이전트 SNMP 포트");

			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			descriptions.put(Locale.ENGLISH, "Port of target snmp agent");
			descriptions.put(Locale.KOREAN, "대상 네트워크 장비의 포트");

			types.add(new StringConfigType(ConfigOption.AgentPort.configKey, displayNames, descriptions, false));
		}
		{
			Map<Locale, String> displayNames = new HashMap<Locale, String>();
			displayNames.put(Locale.ENGLISH, "Community string");
			displayNames.put(Locale.KOREAN, "Community 문자열");

			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			descriptions.put(Locale.ENGLISH, "Community string of target device(ex: public). ");
			descriptions.put(Locale.KOREAN, "대상 네트워크 장비의 Community 문자열(예: public). ");

			types.add(new StringConfigType(ConfigOption.SnmpCommunity.configKey, displayNames, descriptions, true));
		}
		{
			Map<Locale, String> displayNames = new HashMap<Locale, String>();
			displayNames.put(Locale.ENGLISH, "SNMP version");
			displayNames.put(Locale.KOREAN, "SNMP 버전");

			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			descriptions.put(Locale.ENGLISH, "SNMP version accepted by target agent. ");
			descriptions.put(Locale.KOREAN, "대상 장비가 인식하는 SNMP 버전. " + "기본값 v2c");

			types.add(new IntegerConfigType(ConfigOption.SnmpVersion.configKey, displayNames, descriptions, false));
		}

		return types;
	}

	public Logger createLogger(String name, String description, Properties config) {
		SnmpQueryLogger logger = new SnmpQueryLogger("local", name, description, this, config);
		logger.setSnmp(snmp);
		return logger;
	}

	@Override
	protected Logger createLogger(LoggerSpecification spec) {
		 SnmpQueryLogger logger = new SnmpQueryLogger(spec.getNamespace(), spec.getName(), spec.getDescription(), this,
		 spec.getConfig());
		 logger.setSnmp(snmp);
		 return logger;
	}
}
