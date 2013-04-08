package org.krakenapps.logdb;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface CsvLookupRegistry {

	Set<File> getCsvFiles();

	void loadCsvFile(File f) throws IOException;

	void unloadCsvFile(File f);
}
