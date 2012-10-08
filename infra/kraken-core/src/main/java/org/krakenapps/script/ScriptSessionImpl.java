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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.krakenapps.api.ScriptSession;
import org.krakenapps.console.ConsoleHistoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptSessionImpl implements ScriptSession {
	private final Logger logger = LoggerFactory.getLogger(ScriptSessionImpl.class);
	private static final String hostname;

	private ConcurrentMap<String, Object> properties;
	private ConsoleHistoryManager history;

	static {
		String h = "unknown";
		try {
			h = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		}

		hostname = h;
	}

	public ScriptSessionImpl(ConsoleHistoryManager history) {
		this.properties = new ConcurrentHashMap<String, Object>();
		this.history = history;
	}

	@Override
	public String getPrompt() {
		File dir = (File) getProperty("dir");
		String workingDirectory = null;
		try {
			if (dir != null) {
				if (dir.getAbsolutePath().endsWith(File.separator) && dir.getParentFile() != null)
					dir = dir.getParentFile();
				workingDirectory = dir.getCanonicalFile().getName();
			}
		} catch (IOException e) {
			logger.error("kraken core: cannot resolve working directory", e);
		}

		if (workingDirectory == null || workingDirectory.isEmpty())
			workingDirectory = "/";

		return "kraken@" + hostname + " " + workingDirectory + "> ";
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
