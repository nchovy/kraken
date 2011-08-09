package org.krakenapps.logstorage.engine;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.krakenapps.logstorage.engine.v1.LogFileWriterV1;
import org.krakenapps.logstorage.engine.v2.LogFileWriterV2;

public abstract class LogFileWriter {
	public static LogFileWriter getLogFileWriter(File indexPath, File dataPath, String defaultLogVersion)
			throws InvalidLogFileHeaderException, IOException {
		LogFileWriter writer = null;

		if (indexPath.exists() && dataPath.exists()) {
			RandomAccessFile headerReader = new RandomAccessFile(indexPath, "r");
			LogFileHeader header = LogFileHeader.extractHeader(headerReader);
			headerReader.close();

			if (header.version() == 1)
				writer = new LogFileWriterV1(indexPath, dataPath);
			else if (header.version() == 2)
				writer = new LogFileWriterV2(indexPath, dataPath);
		} else if (!indexPath.exists() && dataPath.exists())
			throw new IOException("index file not exists");
		else if (indexPath.exists() && !dataPath.exists())
			throw new IOException("data file not exists");
		else {
			if (defaultLogVersion == null)
				defaultLogVersion = "v2";

			if (defaultLogVersion.equals("v1"))
				writer = new LogFileWriterV1(indexPath, dataPath);
			else if (defaultLogVersion.equals("v2"))
				writer = new LogFileWriterV2(indexPath, dataPath);
			else
				throw new IOException("unknown default log version: " + defaultLogVersion);
		}

		return writer;
	}

	public abstract int getLastKey();

	public abstract Date getLastDate();

	public abstract int getCount();

	public abstract void write(LogRecord data) throws IOException;

	public abstract void write(Collection<LogRecord> data) throws IOException;

	public abstract List<LogRecord> getCache();

	public abstract void flush() throws IOException;

	public abstract void close() throws IOException;
}
