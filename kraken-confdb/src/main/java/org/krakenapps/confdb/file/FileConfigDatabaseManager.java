package org.krakenapps.confdb.file;

import java.io.File;

import org.krakenapps.confdb.ConfigDatabase;

public class FileConfigDatabaseManager {
	private File baseDir;

	public FileConfigDatabaseManager(File baseDir) {
		this.baseDir = baseDir;
	}

	public ConfigDatabase open(String name) {
		return new FileConfigDatabase(baseDir, name);
	}
}
