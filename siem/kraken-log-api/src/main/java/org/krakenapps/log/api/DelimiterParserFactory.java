/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.log.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;

@Component(name = "delimiter-parser-factory")
@Provides
public class DelimiterParserFactory implements LogParserFactory {

	@Override
	public String getName() {
		return "delimiter";
	}

	@Override
	public Collection<Locale> getDisplayNameLocales() {
		return Arrays.asList(Locale.ENGLISH);
	}

	@Override
	public String getDisplayName(Locale locale) {
		return "delimiter parser";
	}

	@Override
	public Collection<Locale> getDescriptionLocales() {
		return Arrays.asList(Locale.ENGLISH);
	}

	@Override
	public String getDescription(Locale locale) {
		return "devide a string into tokens based on the given delimiter and column names";
	}

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		List<LoggerConfigOption> options = new ArrayList<LoggerConfigOption>();
		{
			Map<Locale, String> displayNames = new HashMap<Locale, String>();
			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			displayNames.put(Locale.ENGLISH, "delimiter");
			descriptions.put(Locale.ENGLISH, "delimiter character");
			options.add(new StringConfigType("delimiter", displayNames, descriptions, true));
		}
		{
			Map<Locale, String> displayNames = new HashMap<Locale, String>();
			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			displayNames.put(Locale.ENGLISH, "column headers");
			descriptions.put(Locale.ENGLISH, "separated by comma");
			options.add(new StringConfigType("column_headers", displayNames, descriptions, false));
		}
		{
			Map<Locale, String> displayNames = new HashMap<Locale, String>();
			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			displayNames.put(Locale.ENGLISH, "delimiter target field");
			descriptions.put(Locale.ENGLISH, "delimiter target field name");
			options.add(new StringConfigType("delimiter_target", displayNames, descriptions, false));
		}

		return options;
	}

	@Override
	public LogParser createParser(Properties config) {
		String delimiter = config.getProperty("delimiter");
		if (delimiter == null)
			delimiter = " ";

		String[] columnHeaders = null;
		String h = config.getProperty("column_headers");
		if (h != null)
			columnHeaders = h.split(",");

		String delimiterTarget = config.getProperty("delimiter_target");
		if (delimiterTarget == null)
			return new DelimiterParser(delimiter, columnHeaders);
		else
			return new DelimiterParser(delimiter, columnHeaders, delimiterTarget);
	}
}
