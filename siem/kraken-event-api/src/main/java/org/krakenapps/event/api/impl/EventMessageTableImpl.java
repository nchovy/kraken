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
package org.krakenapps.event.api.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.event.api.EventMessageKey;
import org.krakenapps.event.api.EventMessageTable;

@Component(name = "event-message-table")
@Provides
public class EventMessageTableImpl implements EventMessageTable {
	private ConcurrentMap<EventMessageKey, String> templates;

	@Validate
	public void start() {
		templates = new ConcurrentHashMap<EventMessageKey, String>();
	}

	@Override
	public String formatMessage(EventMessageKey key, Map<String, String> properties) {
		String text = templates.get(key);
		if (text == null)
			return null;

		for (String name : properties.keySet()) {
			String value = properties.get(name);
			text = text.replaceAll("${" + name + "}", value);
		}

		return text;
	}

	@Override
	public String getTemplate(EventMessageKey key) {
		return templates.get(key);
	}

	@Override
	public Set<EventMessageKey> keySet() {
		return templates.keySet();
	}

	@Override
	public void registerTemplate(EventMessageKey key, String template) {
		templates.put(key, template);
	}

	@Override
	public void unregisterTemplate(EventMessageKey key) {
		templates.remove(key);
	}
}
