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
package org.krakenapps.script;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(RegistryScript.class.getName());
	private BundleContext bc;
	private ScriptContext context;

	public RegistryScript(BundleContext bc) {
		this.bc = bc;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "set preference value", arguments = {
			@ScriptArgument(type = "string", name = "space", description = "system or user space name"),
			@ScriptArgument(type = "string", name = "path", description = "node path"),
			@ScriptArgument(type = "string", name = "key", description = "key"),
			@ScriptArgument(type = "string", name = "value", description = "value") })
	public void set(String[] args) {
		String space = args[0];
		String pathName = args[1];
		String key = args[2];
		String value = args[3];

		Preferences prefs = getPreferences(space, pathName);
		if (prefs == null) {
			context.printf("preferences not found: %s, %s\n", space, pathName);
			return;
		}

		prefs.put(key, value);
		context.printf("set space: %s, path:%s, key: %s, value: %s\n", space, pathName, key, value);
	}

	@ScriptUsage(description = "set preference value", arguments = {
			@ScriptArgument(type = "string", name = "space", description = "system or user space name"),
			@ScriptArgument(type = "string", name = "path", description = "node path"),
			@ScriptArgument(type = "string", name = "key", description = "key"),
			@ScriptArgument(type = "string", name = "value", description = "value") })
	public void setLong(String[] args) {
		String space = args[0];
		String pathName = args[1];
		String key = args[2];
		String value = args[3];

		Preferences prefs = getPreferences(space, pathName);
		if (prefs == null) {
			context.printf("preferences not found: %s, %s\n", space, pathName);
			return;
		}

		prefs.putLong(key, Long.parseLong(value));
		context.printf("set space: %s, path:%s, key: %s, value: %s\n", space, pathName, key, value);
	}

	@ScriptUsage(description = "set preference value", arguments = {
			@ScriptArgument(type = "string", name = "space", description = "system or user space name"),
			@ScriptArgument(type = "string", name = "path", description = "node path"),
			@ScriptArgument(type = "string", name = "key", description = "key"),
			@ScriptArgument(type = "string", name = "value", description = "value") })
	public void setInt(String[] args) {
		String space = args[0];
		String pathName = args[1];
		String key = args[2];
		String value = args[3];

		Preferences prefs = getPreferences(space, pathName);
		if (prefs == null) {
			context.printf("preferences not found: %s, %s\n", space, pathName);
			return;
		}

		prefs.putInt(key, Integer.parseInt(value));
		context.printf("set space: %s, path:%s, key: %s, value: %s\n", space, pathName, key, value);
	}

	@ScriptUsage(description = "set preference value", arguments = {
			@ScriptArgument(type = "string", name = "space", description = "system or user space name"),
			@ScriptArgument(type = "string", name = "path", description = "node path"),
			@ScriptArgument(type = "string", name = "key", description = "key"),
			@ScriptArgument(type = "string", name = "value", description = "value") })
	public void setBool(String[] args) {
		String space = args[0];
		String pathName = args[1];
		String key = args[2];
		String value = args[3];

		Preferences prefs = getPreferences(space, pathName);
		if (prefs == null) {
			context.printf("preferences not found: %s, %s\n", space, pathName);
			return;
		}

		prefs.putBoolean(key, Boolean.parseBoolean(value));
		context.printf("set space: %s, path:%s, key: %s, value: %s\n", space, pathName, key, value);
	}

	@ScriptUsage(description = "set preference value", arguments = {
			@ScriptArgument(type = "string", name = "space", description = "system or user space name"),
			@ScriptArgument(type = "string", name = "path", description = "node path"),
			@ScriptArgument(type = "string", name = "key", description = "key"),
			@ScriptArgument(type = "string", name = "value", description = "value") })
	public void setDouble(String[] args) {
		String space = args[0];
		String pathName = args[1];
		String key = args[2];
		String value = args[3];

		Preferences prefs = getPreferences(space, pathName);
		if (prefs == null) {
			context.printf("preferences not found: %s, %s\n", space, pathName);
			return;
		}

		prefs.putDouble(key, Double.parseDouble(value));
		context.printf("set space: %s, path:%s, key: %s, value: %s\n", space, pathName, key, value);
	}

	@ScriptUsage(description = "set preference value", arguments = {
			@ScriptArgument(type = "string", name = "space", description = "system or user space name"),
			@ScriptArgument(type = "string", name = "path", description = "node path"),
			@ScriptArgument(type = "string", name = "key", description = "key"),
			@ScriptArgument(type = "string", name = "value", description = "value") })
	public void setFloat(String[] args) {
		String space = args[0];
		String pathName = args[1];
		String key = args[2];
		String value = args[3];

		Preferences prefs = getPreferences(space, pathName);
		if (prefs == null) {
			context.printf("preferences not found: %s, %s\n", space, pathName);
			return;
		}

		prefs.putFloat(key, Float.parseFloat(value));
		context.printf("set space: %s, path:%s, key: %s, value: %s\n", space, pathName, key, value);
	}

	@ScriptUsage(description = "print preference value", arguments = {
			@ScriptArgument(type = "string", name = "space", description = "system or user space name"),
			@ScriptArgument(type = "string", name = "path", description = "node path"),
			@ScriptArgument(type = "string", name = "key", description = "key") })
	public void print(String[] args) {
		String space = args[0];
		String pathName = args[1];
		String key = args[2];

		Preferences prefs = getPreferences(space, pathName);
		if (prefs == null) {
			context.printf("preferences not found: %s, %s\n", space, pathName);
			return;
		}

		String value = prefs.get(key, null);
		context.printf("value: %s\n", value);
	}

	@ScriptUsage(description = "forces any change of the Preferences object to be saved in the persistent storage", arguments = {
			@ScriptArgument(type = "string", name = "space", description = "system or user space name"),
			@ScriptArgument(type = "string", name = "path", description = "node path")})
	public void flush(String[] args) {
		String space = args[0];
		String pathName = args[1];

		Preferences prefs = getPreferences(space, pathName);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			context.printf("backing store error: %s\n", e.getMessage());
			logger.error("backing store error: {}", e);
		}
	}

	@ScriptUsage(description = "reload the content of the Preferences object from the persistent storage; before reloading, all the contents are saved", arguments = {
			@ScriptArgument(type = "string", name = "space", description = "system or user space name"),
			@ScriptArgument(type = "string", name = "path", description = "node path")})
	public void sync(String[] args) {
		String space = args[0];
		String pathName = args[1];

		Preferences prefs = getPreferences(space, pathName);
		try {
			prefs.sync();
		} catch (BackingStoreException e) {
			context.printf("backing store error: %s\n", e.getMessage());
			logger.error("backing store error: {}", e);
		}
	}

	private Preferences getPreferences(String space, String pathName) {
		PreferencesService preferences = getPreferencesService();
		Preferences prefs = null;

		if (space.equalsIgnoreCase("system")) {
			prefs = preferences.getSystemPreferences();
		} else {
			prefs = preferences.getUserPreferences(space);
		}

		if (prefs == null)
			return null;

		return prefs.node(pathName);
	}

	private PreferencesService getPreferencesService() {
		ServiceReference ref = bc.getServiceReference(PreferencesService.class.getName());
		if (ref == null) {
			System.out.println("preferences service not found!!!");
			return null;
		}

		return (PreferencesService) bc.getService(ref);
	}
}
