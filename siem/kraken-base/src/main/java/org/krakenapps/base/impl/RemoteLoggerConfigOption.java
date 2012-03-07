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

import org.krakenapps.log.api.LoggerConfigOption;

public class RemoteLoggerConfigOption implements LoggerConfigOption {
	private String name;
	private String type;
	private boolean isRequired;
	private Map<Locale, String> displayNames;
	private Map<Locale, String> descriptions;
	private Map<Locale, String> defaultValues;

	public RemoteLoggerConfigOption(String name, String type, boolean isRequired, Map<Locale, String> displayNames,
			Map<Locale, String> descriptions, Map<Locale, String> defaultValues) {
		this.name = name;
		this.type = type;
		this.isRequired = isRequired;
		this.displayNames = displayNames;
		this.descriptions = descriptions;
		this.defaultValues = defaultValues;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getDescription(Locale locale) {
		return descriptions.get(locale);
	}

	@Override
	public Collection<Locale> getDescriptionLocales() {
		return Collections.unmodifiableCollection(descriptions.keySet());
	}

	@Override
	public String getDisplayName(Locale locale) {
		return displayNames.get(locale);
	}

	@Override
	public Collection<Locale> getDisplayNameLocales() {
		return Collections.unmodifiableCollection(displayNames.keySet());
	}

	@Override
	public boolean isRequired() {
		return isRequired;
	}

	@Override
	public Collection<Locale> getDefaultValueLocales() {
		return defaultValues.keySet();
	}

	@Override
	public String getDefaultValue(Locale locale) {
		return defaultValues.get(locale);
	}

	@Override
	public Object parse(String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void validate(Object value) {
		throw new UnsupportedOperationException();
	}

}
