package org.krakenapps.siem.engine;

import java.io.File;
import java.io.IOException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.file.FileConfigDatabase;
import org.krakenapps.msgbus.Session;
import org.krakenapps.siem.ConfigManager;

@Component(name = "siem-config-manager")
@Provides
public class ConfigManagerImpl implements ConfigManager {

	private ConfigDatabase db;

	@Validate
	public void start() throws IOException {
		File dataDir = new File(System.getProperty("kraken.data.dir"));
		File baseDir = new File(dataDir, "kraken-siem");
		baseDir.mkdirs();

		db = new FileConfigDatabase(baseDir, "siem");
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
