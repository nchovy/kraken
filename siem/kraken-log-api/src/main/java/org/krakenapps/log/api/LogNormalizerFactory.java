package org.krakenapps.log.api;

import java.util.Collection;
import java.util.Locale;
import java.util.Properties;

public interface LogNormalizerFactory {
	String getName();

	Collection<Locale> getDisplayNameLocales();

	String getDisplayName(Locale locale);

	Collection<Locale> getDescriptionLocales();

	String getDescription(Locale locale);

	Collection<LoggerConfigOption> getConfigOptions();

	LogNormalizer createNormalizer(Properties config);

}
