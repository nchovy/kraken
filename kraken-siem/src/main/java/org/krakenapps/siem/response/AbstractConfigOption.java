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
package org.krakenapps.siem.response;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class AbstractConfigOption implements ResponseConfigOption {
	private String name;
	private Map<Locale, String> displayNames;
	private Map<Locale, String> descriptions;
	private boolean required = true;

	public AbstractConfigOption(String name, String displayName, String description) {
		this(name, displayName, description, false);
	}

	public AbstractConfigOption(String name, String displayName, String description, boolean isOptional) {
		this.displayNames = new HashMap<Locale, String>();
		this.descriptions = new HashMap<Locale, String>();
		this.name = name;
		this.required = !isOptional;
		this.displayNames.put(Locale.ENGLISH, displayName);
		this.descriptions.put(Locale.ENGLISH, description);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName(Locale locale) {
		String displayName = displayNames.get(locale);
		if (displayName != null)
			return displayName;

		return displayNames.get(locale);
	}

	@Override
	public String getDescription(Locale locale) {
		String description = descriptions.get(locale);
		if (description != null)
			return description;

		return descriptions.get(Locale.ENGLISH);
	}

	public void addDisplayName(Locale locale, String displayName) {
		displayNames.put(locale, displayName);
	}

	public void addDescription(Locale locale, String description) {
		descriptions.put(locale, description);
	}

	@Override
	public String getType() {
		return "string";
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public Object parse(String value) {
		return value;
	}

	@Override
	public Map<String, Object> marshal() {
		return marshal(Locale.ENGLISH);
	}

	@Override
	public Map<String, Object> marshal(Locale locale) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", name);
		m.put("description", getDescription(locale));
		m.put("display_name", getDisplayName(locale));
		m.put("type", getType());
		m.put("required", required);
		return m;
	}
}
