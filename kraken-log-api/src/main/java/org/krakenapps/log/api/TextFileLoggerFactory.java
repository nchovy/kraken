package org.krakenapps.log.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;

@Component(name = "text-file-logger-factory")
@Provides
public class TextFileLoggerFactory extends AbstractLoggerFactory {
	private final List<LoggerConfigOption> options;

	public TextFileLoggerFactory() {
		options = new ArrayList<LoggerConfigOption>();
		options.add(new StringConfigType("file.path", map("File Path"), map("Log file path"), true, map("/var/log/")));
		options.add(new StringConfigType("date.extractor", map("Date Extractor"), map("Regex for date extraction"),
				false, map(null)));
		options.add(new StringConfigType("date.pattern", map("Date Pattern"), map("Date pattern of log file"), false,
				map("MMM dd HH:mm:ss")));
		options.add(new StringConfigType("date.locale", map("Date Locale"), map("Date locale of log file"), false,
				map("en")));
	}

	private Map<Locale, String> map(String value) {
		Map<Locale, String> m = new HashMap<Locale, String>();
		m.put(Locale.ENGLISH, value);
		return m;
	}

	@Override
	public String getName() {
		return "textfile";
	}

	@Override
	protected Logger createLogger(LoggerSpecification spec) {
		try {
			return new TextFileLogger(spec, this);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String getDescription(Locale locale) {
		return "text file logger";
	}

	@Override
	public String getDisplayName(Locale locale) {
		return "text file logger";
	}

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		return options;
	}
}
