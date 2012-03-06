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
