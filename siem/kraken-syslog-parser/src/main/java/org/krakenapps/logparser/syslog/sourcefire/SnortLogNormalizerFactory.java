package org.krakenapps.logparser.syslog.sourcefire;

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

@Component(name = "snort-log-normalizer-factory")
@Provides
public class SnortLogNormalizerFactory implements LogNormalizerFactory {

	@Override
	public String getName() {
		return "snort";
	}

	@Override
	public Collection<Locale> getDisplayNameLocales() {
		return Arrays.asList(Locale.ENGLISH);
	}

	@Override
	public String getDisplayName(Locale locale) {
		return "snort log normalizer";
	}

	@Override
	public Collection<Locale> getDescriptionLocales() {
		return Arrays.asList(Locale.ENGLISH);
	}

	@Override
	public String getDescription(Locale locale) {
		return "snort log normalizer";
	}

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		return new ArrayList<LoggerConfigOption>();
	}

	@Override
	public LogNormalizer createNormalizer(Properties config) {
		return new SnortLogNormalizer();
	}

}
