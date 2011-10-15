package org.krakenapps.confdb.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.krakenapps.confdb.CommitOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionLogWriter {
	private final Logger logger = LoggerFactory.getLogger(CollectionLogReader.class.getName());
	private static final int COL_LOG_SIZE = 34;

	/**
	 * collection log file handle
	 */
	private RandomAccessFile logRaf;

	/**
	 * doc file handle
	 */
	private RandomAccessFile datRaf;

	/**
	 * collection log buffer
	 */
	private byte[] buffer;

	public CollectionLogWriter(File logFile, File datFile) throws IOException {
		this.logRaf = new RandomAccessFile(logFile, "rw");
		this.datRaf = new RandomAccessFile(datFile, "rw");
		this.buffer = new byte[COL_LOG_SIZE];

		// TODO: check signature and collection metadata (e.g. version, name)
	}

	public int write(CollectionLog log) throws IOException {
		byte[] doc = log.getDoc();

		// append doc binary data
		long datSize = datRaf.length();
		datRaf.seek(datSize);
		datRaf.writeInt(doc == null ? 0 : doc.length);
		datRaf.writeInt(0); // option
		if (doc != null)
			datRaf.write(doc);

		// append collection log
		log.setDocOffset(datSize);
		log.setDocLength(doc == null ? 0 : doc.length);

		long logSize = logRaf.length();
		if (log.getOperation() == CommitOp.CreateDoc)
			log.setDocId((int) (logSize / COL_LOG_SIZE) + 1);

		ByteBuffer bb = ByteBuffer.wrap(buffer);
		log.serialize(bb);
		logRaf.seek(logSize);
		logRaf.write(bb.array());

		sync();

		return log.getDocId();
	}

	private void sync() throws IOException {
		datRaf.getFD().sync();
		logRaf.getFD().sync();
	}

	public void close() {
		try {
			logRaf.close();
		} catch (IOException e) {
			logger.error("kraken confdb: cannot close collection log file", e);
		}

		try {
			datRaf.close();
		} catch (IOException e) {
			logger.error("kraken confdb: cannot close doc file", e);
		}
	}

}
