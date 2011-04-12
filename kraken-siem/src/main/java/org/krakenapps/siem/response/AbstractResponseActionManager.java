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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractResponseActionManager implements ResponseActionManager {
	private final Logger logger = LoggerFactory.getLogger(AbstractResponseActionManager.class.getName());
	private ConcurrentMap<String, ResponseAction> actionMap;
	private CopyOnWriteArraySet<ResponseActionManagerEventListener> callbacks;

	public AbstractResponseActionManager() {
		this.actionMap = new ConcurrentHashMap<String, ResponseAction>();
		this.callbacks = new CopyOnWriteArraySet<ResponseActionManagerEventListener>();
	}

	@Override
	public Collection<ResponseAction> getActions() {
		return Collections.unmodifiableCollection(actionMap.values());
	}

	@Override
	public Collection<ResponseAction> getActions(String namespace) {
		Set<ResponseAction> actions = new HashSet<ResponseAction>();

		for (String key : actionMap.keySet())
			if (key.startsWith(namespace + "\\"))
				actions.add(actionMap.get(key));

		return actions;
	}

	@Override
	public ResponseAction getAction(String namespace, String name) {
		return actionMap.get(namespace + "\\" + name);
	}

	@Override
	public Collection<ResponseConfigOption> getConfigOptions() {
		return new ArrayList<ResponseConfigOption>();
	}

	@Override
	public String getDisplayName(Locale locale) {
		return "";
	}

	@Override
	public String getDescription(Locale locale) {
		return "";
	}

	@Override
	public ResponseAction newAction(String namespace, String name, String description, Properties config) {
		String fullName = namespace + "\\" + name;

		ResponseAction action = createResponseAction(namespace, name, description, config);
		ResponseAction old = actionMap.putIfAbsent(fullName, action);
		if (old != null)
			throw new IllegalStateException("duplicated action name exists: " + fullName);

		for (ResponseActionManagerEventListener callback : callbacks) {
			try {
				callback.actionCreated(this, action);
			} catch (Exception e) {
				logger.error("kraken siem: response manager event listener should not throw any exception", e);
			}
		}

		return action;
	}

	@Override
	public void deleteAction(String namespace, String name) {
		ResponseAction action = actionMap.remove(namespace + "\\" + name);
		if (action == null)
			return;

		for (ResponseActionManagerEventListener callback : callbacks) {
			try {
				callback.actionRemoved(this, action);
			} catch (Exception e) {
				logger.error("kraken siem: response manager event listener should not throw any exception", e);
			}
		}
	}

	protected abstract ResponseAction createResponseAction(String namespace, String name, String description,
			Properties config);

	@Override
	public void addEventListener(ResponseActionManagerEventListener callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeEventListener(ResponseActionManagerEventListener callback) {
		callbacks.remove(callback);
	}

	@Override
	public String toString() {
		return getName();
	}
}
