package org.krakenapps.logparser.syslog.juniper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.log.api.LogParser;
import org.krakenapps.log.api.LogParserFactory;
import org.krakenapps.log.api.LoggerConfigOption;

@Component(name = "netscreen-log-parser-factory")
@Provides
public class NetScreenLogParserFactory implements LogParserFactory {
	@Override
	public String getName() {
		return "netscreen-isg";
	}

	@Override
	public Collection<Locale> getDisplayNameLocales() {
		return Arrays.asList(Locale.ENGLISH);
	}

	@Override
	public String getDisplayName(Locale locale) {
		return "NetScreen Log Parser Factory";
	}

	@Override
	public Collection<Locale> getDescriptionLocales() {
		return Arrays.asList(Locale.ENGLISH);
	}

	@Override
	public String getDescription(Locale locale) {
		return "Create netscreen log parser";
	}

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		return new ArrayList<LoggerConfigOption>();
	}

	@Override
	public LogParser createParser(Properties config) {
		return new NetScreenLogParser();
	}
}
