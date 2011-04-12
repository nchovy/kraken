/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.script;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.krakenapps.api.ScriptSession;
import org.krakenapps.console.ConsoleHistoryManager;

public class ScriptSessionImpl implements ScriptSession {
	private ConcurrentMap<String, Object> properties;
	private ConsoleHistoryManager history;

	public ScriptSessionImpl(ConsoleHistoryManager history) {
		this.properties = new ConcurrentHashMap<String, Object>();
		this.history = history;
	}

	@Override
	public Object getProperty(String key) {
		return properties.get(key);
	}

	@Override
	public Set<String> getPropertyKeys() {
		return properties.keySet();
	}

	@Override
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	@Override
	public Object setPropertyIfAbsent(String key, Object value) {
		return properties.putIfAbsent(key, value);
	}

	@Override
	public void unsetProperty(String key) {
		properties.remove(key);
	}

	@Override
	public Collection<String> getCommandHistory() {
		return history.getCommandHistory();
	}

}
