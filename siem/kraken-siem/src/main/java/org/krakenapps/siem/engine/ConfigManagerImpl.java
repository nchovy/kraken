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
package org.krakenapps.siem.engine;

import java.io.IOException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.msgbus.Session;
import org.krakenapps.siem.ConfigManager;

@Component(name = "siem-config-manager")
@Provides
public class ConfigManagerImpl implements ConfigManager {
	@Requires
	private ConfigService conf;

	private ConfigDatabase db;

	@Validate
	public void start() throws IOException {
		db = conf.ensureDatabase("kraken-siem");
	}

	@Override
	public ConfigDatabase getDatabase(Session session) {
		return db;
	}

	@Override
	public ConfigDatabase getDatabase() {
		return db;
	}

}
