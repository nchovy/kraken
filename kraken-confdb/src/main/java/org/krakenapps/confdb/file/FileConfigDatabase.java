package org.krakenapps.confdb.file;

import java.io.File;

import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;

public class FileConfigDatabase implements ConfigDatabase {

	private File baseDir;
	private String name;

	public FileConfigDatabase(File baseDir, String name) {
		this.baseDir = baseDir;
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ConfigCollection ensureCollection(String name) {
		return null;
	}

	@Override
	public void dropCollection(String name) {

	}

}
