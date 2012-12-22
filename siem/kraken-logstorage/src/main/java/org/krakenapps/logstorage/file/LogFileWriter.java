/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.logstorage.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public abstract class LogFileWriter {
	public static LogFileWriter getLogFileWriter(File indexPath, File dataPath, String defaultLogVersion)
			throws InvalidLogFileHeaderException, IOException {
		LogFileWriter writer = null;

		if (indexPath.exists() && dataPath.exists()) {
			RandomAccessFile headerReader = new RandomAccessFile(indexPath, "r");
			LogFileHeader header = LogFileHeader.extractHeader(headerReader, indexPath);
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

	public abstract long getLastKey();

	public abstract Date getLastDate();

	public abstract Date getLastFlush();

	public abstract long getCount();

	public abstract void write(LogRecord data) throws IOException;

	public abstract void write(Collection<LogRecord> data) throws IOException;

	public abstract List<LogRecord> getBuffer();

	public abstract void flush() throws IOException;

	public abstract void close() throws IOException;
}
