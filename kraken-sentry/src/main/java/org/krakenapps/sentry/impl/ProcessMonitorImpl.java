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
package org.krakenapps.sentry.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.sentry.process.ProcessCheck;
import org.krakenapps.sentry.process.ProcessCheckEventListener;
import org.krakenapps.sentry.process.ProcessCheckOption;
import org.krakenapps.sentry.process.ProcessMonitor;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "process-monitor")
@Provides
public class ProcessMonitorImpl implements ProcessMonitor {
	private static final String PROCESS_CHECK_KEY = "process_check";

	private final Logger logger = LoggerFactory.getLogger(ProcessMonitorImpl.class.getName());

	private Map<String, ProcessCheck> checklist;
	private Set<ProcessCheckEventListener> callbacks;

	@Requires
	private PreferencesService prefsvc;

	@Validate
	public void start() {
		checklist = new ConcurrentHashMap<String, ProcessCheck>();
		callbacks = Collections.newSetFromMap(new ConcurrentHashMap<ProcessCheckEventListener, Boolean>());

		// load saved checklist
		try {
			Preferences root = getConfig();
			for (String name : root.childrenNames()) {
				String policy = root.node(name).get("policy", null);
				ProcessCheckOption option = ProcessCheckOption.valueOf(policy);
				checklist.put(name, new ProcessCheck(name, option));
			}
		} catch (BackingStoreException e) {
			logger.error("kraken sentry: cannot load process checklist", e);
		}
	}

	@Invalidate
	public void stop() {
		checklist.clear();
	}

	@Override
	public Collection<ProcessCheck> getProcessChecklist() {
		return checklist.values();
	}

	@Override
	public void addProcess(String processName, ProcessCheckOption option) {
		if (processName == null)
			throw new IllegalArgumentException("process name must be not null");

		// add to persistent config
		try {
			Preferences p = getConfig();
			Preferences node = p.node(processName.toLowerCase());
			node.put("policy", option.toString());
			p.flush();
			p.sync();

			// add to checklist
			checklist.put(processName, new ProcessCheck(processName, option));
		} catch (BackingStoreException e) {
			logger.error("kraken sentry: cannot save process check", e);
		}
	}

	@Override
	public void removeProcess(String processName) {
		// remove persistent config
		try {
			Preferences p = getConfig();
			if (!p.nodeExists(processName.toLowerCase()))
				throw new IllegalStateException(processName + " not found");

			p.node(processName.toLowerCase()).removeNode();
			p.flush();
			p.sync();

			// remove from checklist
			checklist.remove(processName);
		} catch (BackingStoreException e) {
			logger.error("kraken sentry: cannot remove process check", e);
		}
	}

	@Override
	public void addListener(ProcessCheckEventListener callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeListener(ProcessCheckEventListener callback) {
		callbacks.remove(callback);
	}

	@Override
	public void dispatch(String processName, ProcessCheckOption option, boolean isRunning) {
		for (ProcessCheckEventListener callback : callbacks) {
			try {
				callback.onCheck(processName, option, isRunning);
			} catch (Exception e) {
				logger.warn("kraken sentry: process check event listener should not throw any exception", e);
			}
		}
	}

	private Preferences getConfig() {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			if (!root.nodeExists(PROCESS_CHECK_KEY)) {
				Preferences p = root.node(PROCESS_CHECK_KEY);
				p.flush();
				p.sync();
			}

			return root.node(PROCESS_CHECK_KEY);
		} catch (BackingStoreException e) {
			logger.error("kraken sentry: cannot fetch config store", e);
			throw new IllegalStateException("cannot fetch process check config");
		}
	}
}
