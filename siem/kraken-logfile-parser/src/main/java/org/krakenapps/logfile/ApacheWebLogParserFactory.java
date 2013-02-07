/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.logfile;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.log.api.LogParser;
import org.krakenapps.log.api.LogParserFactory;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.log.api.StringConfigType;

@Component(name = "httpd-log-parser-factory")
@Provides
public class ApacheWebLogParserFactory implements LogParserFactory {
	@Override
	public String getName() {
		return "httpd";
	}

	@Override
	public LogParser createParser(Properties config) {
		String t = config.getProperty("log_format");
		if (t != null)
			return new ApacheWebLogParser(t);
		return new ApacheWebLogParser();
	}

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		LoggerConfigOption s = new StringConfigType("log_format", text("Log Format"), text("Apache Log Format"), true,
				text("%h %l %u %t \"%r\" %>s %O \"%{Referer}i\" \"%{User-Agent}i\""));
		return Arrays.asList(s);
	}

	private Map<Locale, String> text(String text) {
		Map<Locale, String> m = new HashMap<Locale, String>();
		m.put(Locale.ENGLISH, text);
		return m;
	}

	@Override
	public String getDisplayName(Locale locale) {
		return "Apache Web Log Parser Factory";
	}

	@Override
	public Collection<Locale> getDisplayNameLocales() {
		return Arrays.asList(Locale.ENGLISH);
	}

	@Override
	public String getDescription(Locale locale) {
		return "Create apache httpd log parser with log format option";
	}

	@Override
	public Collection<Locale> getDescriptionLocales() {
		return Arrays.asList(Locale.ENGLISH);
	}

}
