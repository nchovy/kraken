package org.krakenapps.logstorage.engine;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.logstorage.LogRestoreService;
import org.krakenapps.logstorage.LogTableRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "logstorage-restore-service")
@Provides
public class LogRestoreServiceImpl implements LogRestoreService {
	private Logger logger = LoggerFactory.getLogger(LogRestoreServiceImpl.class);
	private File logDir = new File(System.getProperty("kraken.data.dir"), "kraken-logstorage/log");

	@Requires
	private LogTableRegistry tableRegistry;

	@Override
	public void restoreByDelete() {
		for (String tableName : tableRegistry.getTableNames())
			restoreByDelete(tableName);
	}

	@Override
	public void restoreByDelete(String tableName) {
		if (!logDir.exists() || !logDir.isDirectory())
			throw new IllegalStateException("does not exist log directory, path[" + logDir.getAbsolutePath() + "]");

		File tableDir = new File(logDir, Integer.toString(tableRegistry.getTableId(tableName)));
		if (!tableDir.exists())
			return;

		String pattern = "\\d{4}[-]\\d{2}[-]\\d{2}\\.(dat|idx)";
		Set<String> removeFileNames = new HashSet<String>();
		for (File f : tableDir.listFiles()) {
			// check log file name format
			if (!Pattern.matches(pattern, f.getName()))
				continue;

			// check log file size
			if (f.length() > 40)
				continue;

			removeFileNames.add(f.getName().substring(0, 10));
		}

		if (removeFileNames.size() == 0) {
			logger.trace("kraken logstorage: cannot find invalid log file, table name[{}]", tableName);
			return;
		}

		for (String fileName : removeFileNames) {
			File removeIdxFile = new File(tableDir, fileName + ".idx");
			if (removeIdxFile.exists()) {
				logger.info("kraken logstorage: remove invalid log index file [{}]", removeIdxFile.getName());
				removeIdxFile.delete();
			}
			File removeDatFile = new File(tableDir, fileName + ".dat");
			if (removeDatFile.exists()) {
				logger.info("kraken logstorage: remove invalid log data file [{}]", removeDatFile.getName());
				removeDatFile.delete();
			}
		}
	}

	@Override
	public void restoreByFixHeader() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void restoreByFixHeader(String tableName) {
		throw new UnsupportedOperationException();
	}

}
