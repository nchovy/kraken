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

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class AbstractConfigType implements LoggerConfigOption {
	protected String name;
	protected Map<Locale, String> displayNames;
	protected Map<Locale, String> descriptions;
	protected boolean isRequired;
	protected Map<Locale, String> defaultValues;

	public AbstractConfigType(String name, Map<Locale, String> displayNames, Map<Locale, String> descriptions,
			boolean isRequired) {
		this(name, displayNames, descriptions, isRequired, new HashMap<Locale, String>());
	}

	public AbstractConfigType(String name, Map<Locale, String> displayNames, Map<Locale, String> descriptions,
			boolean isRequired, Map<Locale, String> defaultValues) {
		this.name = name;
		this.displayNames = displayNames;
		this.descriptions = descriptions;
		this.isRequired = isRequired;
		this.defaultValues = defaultValues;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName(Locale locale) {
		String displayName = displayNames.get(locale);
		if (displayName == null)
			return displayNames.get(locale);
		return displayName;
	}

	@Override
	public String getDescription(Locale locale) {
		String description = descriptions.get(locale);
		if (description == null)
			return displayNames.get(Locale.ENGLISH);

		return description;
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
	public Collection<Locale> getDescriptionLocales() {
		return descriptions.keySet();
	}

	@Override
	public Collection<Locale> getDisplayNameLocales() {
		return displayNames.keySet();
	}

	@Override
	public void validate(Object value) {
		// override this for validation
	}
}
