package org.krakenapps.snmpmon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.log.api.AbstractLoggerFactory;
import org.krakenapps.log.api.IntegerConfigType;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.log.api.LoggerSpecification;
import org.krakenapps.log.api.StringConfigType;

@Component(name = "snmp-network-usage-logger-factory")
@Provides
public class AgentLoggerFactory extends AbstractLoggerFactory {
	@Override
	public String getName() {
		return "network-usage";
	}

	@Override
	public Collection<Locale> getDisplayNameLocales() {
		ArrayList<Locale> locales = new ArrayList<Locale>();
		locales.add(Locale.ENGLISH);
		return locales;
	}

	@Override
	public String getDisplayName(Locale locale) {
		return "Network usage logger";
	}

	@Override
	public Collection<Locale> getDescriptionLocales() {
		ArrayList<Locale> locales = new ArrayList<Locale>();
		locales.add(Locale.ENGLISH);
		return locales;
	}

	@Override
	public String getDescription(Locale locale) {
		return "It generates logs by querying network agents with SNMP";
	}

	public enum ConfigOption {
		target("target"),
		community("community"),
		version("version"),
		agents("agents"), 
		hostId("hostId"), 
		port("port");
		
		String configKey;

		ConfigOption(String configKey) {
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
			displayNames.put(Locale.ENGLISH, "Targets(IP:PORT format)");
			displayNames.put(Locale.KOREAN, "대상 에이전트 주소(IP:PORT 형식)");

			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			descriptions.put(Locale.ENGLISH, "Addresses of target network agents");
			descriptions.put(Locale.KOREAN, "대상 네트워크 장비의 주소들");

			types.add(new StringConfigType(ConfigOption.target.toString(), displayNames, descriptions, true));
		}

		{
			Map<Locale, String> displayNames = new HashMap<Locale, String>();
			displayNames.put(Locale.ENGLISH, "HostID");

			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			descriptions.put(Locale.ENGLISH, "HostID of target network agents");

			types.add(new IntegerConfigType(ConfigOption.hostId.toString(), displayNames, descriptions, true));
		}

		{
			Map<Locale, String> displayNames = new HashMap<Locale, String>();
			displayNames.put(Locale.ENGLISH, "Community string");
			displayNames.put(Locale.KOREAN, "Community 문자열");

			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			descriptions.put(Locale.ENGLISH, 
					"Community string of target device(ex: public). ");
			descriptions.put(Locale.KOREAN, 
					"대상 네트워크 장비의 Community 문자열(예: public). ");

			types.add(new StringConfigType(ConfigOption.community.toString(), displayNames, descriptions, true));
		}
		
		{
			Map<Locale, String> displayNames = new HashMap<Locale, String>();
			displayNames.put(Locale.ENGLISH, "SNMP version");
			displayNames.put(Locale.KOREAN, "SNMP 버전");

			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			descriptions.put(Locale.ENGLISH, 
					"SNMP version accepted by target agent. "
					);
			descriptions.put(Locale.KOREAN, 
					"대상 장비가 인식하는 SNMP 버전. " +
					"기본값 v2c");

			types.add(new IntegerConfigType(ConfigOption.version.toString(), displayNames, descriptions, false));
		}

		return types;
	}

	public Logger createLogger(String name, String description, Properties config) {
		return new AgentLogger("local", name, description, this, config);	
	}
	
	@Override
	protected Logger createLogger(LoggerSpecification spec) {
		return new AgentLogger(spec.getNamespace(), spec.getName(), spec.getDescription(), this, spec.getConfig());
	}
}
