package org.krakenapps.logstorage;

import java.io.File;

public class FileSet {
	private File indexFile;
	private File dataFile;

	public File getIndexFile() {
		return indexFile;
	}

	public void setIndexFile(File indexFile) {
		this.indexFile = indexFile;
	}

	public File getDataFile() {
		return dataFile;
	}

	public void setDataFile(File dataFile) {
		this.dataFile = dataFile;
	}
}
