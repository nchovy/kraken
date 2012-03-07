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
package org.krakenapps.sentry.windows.logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.log.api.AbstractLoggerFactory;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.log.api.LoggerSpecification;
import org.krakenapps.log.api.StringConfigType;

@Component(name = "windows-event-logger-factory")
@Provides
public class WindowsEventLoggerFactory extends AbstractLoggerFactory {
	@Override
	public String getName() {
		return "windows-event-logger";
	}

	@Override
	public Collection<Locale> getDescriptionLocales() {
		return Arrays.asList(Locale.ENGLISH);
	}

	@Override
	public Collection<Locale> getDisplayNameLocales() {
		ArrayList<Locale> locales = new ArrayList<Locale>();
		locales.add(Locale.ENGLISH);
		return locales;
	}

	@Override
	public String getDisplayName(Locale locale) {
		return "Windows EventLog";
	}

	@Override
	public String getDescription(Locale locale) {
		return "Windows EventLog";
	}

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		List<LoggerConfigOption> types = new ArrayList<LoggerConfigOption>();

		Map<Locale, String> displayNames = new HashMap<Locale, String>();
		displayNames.put(Locale.ENGLISH, "Event Source");
		displayNames.put(Locale.KOREAN, "이벤트 소스");

		Map<Locale, String> descriptions = new HashMap<Locale, String>();
		descriptions.put(Locale.ENGLISH, "Event Source");
		descriptions.put(Locale.KOREAN, "이벤트 소스");

		types.add(new StringConfigType("event_source", displayNames, descriptions, true));
		return types;
	}

	@Override
	public Logger createLogger(LoggerSpecification spec) {
		return new WindowsEventLogger(spec, this);
	}

}
