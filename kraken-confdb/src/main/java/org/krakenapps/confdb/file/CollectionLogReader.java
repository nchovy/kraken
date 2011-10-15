package org.krakenapps.confdb.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CollectionLogReader {
	private static final int COL_LOG_SIZE = 34;

	private final Logger logger = LoggerFactory.getLogger(CollectionLogReader.class.getName());

	private FileConfigDatabase db;
	
	private FileConfigCollection col;
	
	/**
	 * collection log file handle
	 */
	private RandomAccessFile logRaf;

	/**
	 * doc file handle
	 */
	private RandomAccessFile docRaf;

	/**
	 * collection log buffer
	 */
	private byte[] buffer;

	public CollectionLogReader(FileConfigDatabase db, FileConfigCollection col, File logFile, File docFile)
			throws FileNotFoundException {
		this.logRaf = new RandomAccessFile(logFile, "r");
		this.docRaf = new RandomAccessFile(docFile, "r");
		this.buffer = new byte[COL_LOG_SIZE];
	}
	
	public ConfigDatabase getDatabase() {
		return db;
	}
	
	public ConfigCollection getCollection() {
		return col;
	}

	public long count() throws IOException {
		// TODO: consider file header size
		return logRaf.length() / COL_LOG_SIZE;
	}

	public CollectionLog findRev(long rev) throws IOException {
		for (long i = count() - 1; i >= 0; i--) {
			CollectionLog log = read(i);
			if (log.getRev() == rev)
				return log;
		}

		return null;
	}

	public CollectionLog read(long index) throws IOException {
		// TODO: consider file header size

		logRaf.seek(index * COL_LOG_SIZE);
		logRaf.read(buffer);
		ByteBuffer bb = ByteBuffer.wrap(buffer);
		return CollectionLog.deserialize(bb);
	}

	public byte[] readDoc(long offset, int length) throws IOException {
		byte[] buf = new byte[length];

		docRaf.seek(offset);
		docRaf.readInt(); // len
		docRaf.readInt(); // opt
		docRaf.read(buf);

		return buf;
	}

	public void close() {
		try {
			logRaf.close();
			docRaf.close();
		} catch (IOException e) {
			logger.error("kraken confdb: cannot close index file", e);
		}
	}
}
