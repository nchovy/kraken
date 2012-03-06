package org.krakenapps.syslog.parser.juniper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.log.api.LogNormalizer;
import org.krakenapps.log.api.LogNormalizerFactory;
import org.krakenapps.log.api.LoggerConfigOption;

@Component(name = "netscreen-log-normalizer")
@Provides
public class NetScreenLogNormalizerFactory implements LogNormalizerFactory {

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
		return "netscreen isc normalizer";
	}

	@Override
	public Collection<Locale> getDescriptionLocales() {
		return Arrays.asList(Locale.ENGLISH);
	}

	@Override
	public String getDescription(Locale locale) {
		return "netscreen isg normalizer";
	}

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		return new ArrayList<LoggerConfigOption>();
	}

	@Override
	public LogNormalizer createNormalizer(Properties config) {
		return new NetScreenLogNormalizer();
	}

}
