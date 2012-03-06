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
package org.krakenapps.base.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.krakenapps.base.RemoteLoggerFactoryInfo;
import org.krakenapps.log.api.LoggerConfigOption;

public class RemoteLoggerFactoryInfoImpl implements RemoteLoggerFactoryInfo {
	private String name;
	private Collection<LoggerConfigOption> options;
	private Map<Locale, String> descriptions;
	private Map<Locale, String> displayNames;

	public RemoteLoggerFactoryInfoImpl(String name, Collection<LoggerConfigOption> options,
			Map<Locale, String> displayNames, Map<Locale, String> descriptions) {
		this.name = name;
		this.options = options;
		this.descriptions = descriptions;
		this.displayNames = displayNames;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		return Collections.unmodifiableCollection(options);
	}

	@Override
	public Collection<Locale> getDescriptionLocales() {
		return Collections.unmodifiableCollection(descriptions.keySet());
	}

	@Override
	public Collection<Locale> getDisplayNameLocales() {
		return Collections.unmodifiableCollection(displayNames.keySet());
	}

	@Override
	public String getDescription(Locale locale) {
		String desc = descriptions.get(locale);
		if (desc == null)
			return descriptions.get(Locale.ENGLISH);

		return desc;
	}

	@Override
	public String getDisplayName(Locale locale) {
		String displayName = displayNames.get(locale);
		if (displayName == null)
			return displayNames.get(Locale.ENGLISH);
		
		return displayName;
	}

	@Override
	public String toString() {
		return String.format("name=%s, type=%s, description=%s", getName(), getDisplayName(Locale.ENGLISH),
				getDescription(Locale.ENGLISH));
	}

}
