/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.logdb.sort;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import org.krakenapps.codec.FastEncodingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RunOutput {
	private static final int WRITE_BUFFER_SIZE = 1024 * 1024 * 8;
	private final Logger logger = LoggerFactory.getLogger(RunOutput.class);
	public BufferedOutputStream dataBos;

	private int written;
	private Run run;
	private BufferedOutputStream indexBos;
	private FileOutputStream indexFos;
	private FileOutputStream dataFos;
	private byte[] intbuf = new byte[4];
	private byte[] longbuf = new byte[8];
	private long dataOffset;
	private boolean noIndexWrite;
	private FastEncodingRule enc = new FastEncodingRule();

	public RunOutput(int id, int length, AtomicInteger cacheCount) throws IOException {
		this(id, length, cacheCount, false);
	}

	public RunOutput(int id, int length, AtomicInteger cacheCount, boolean noIndexWrite) throws IOException {
		this.noIndexWrite = noIndexWrite;

		int remainCacheSize = cacheCount.addAndGet(-length);
		if (remainCacheSize >= 0) {
			this.run = new Run(id, new LinkedList<Item>());
		} else {
			cacheCount.addAndGet(length);

			File indexFile = null;
			File tmpDir = new File(System.getProperty("kraken.data.dir"), "kraken-logdb/sort");
			tmpDir.mkdirs();
			File dataFile = File.createTempFile("run", ".dat", tmpDir);
			if (!noIndexWrite) {
				indexFile = File.createTempFile("run", ".idx", tmpDir);
				logger.debug("kraken logdb: creating run output index [{}]", indexFile.getAbsolutePath());
				indexFos = new FileOutputStream(indexFile);
				indexBos = new BufferedOutputStream(indexFos, WRITE_BUFFER_SIZE);
			}
			dataFos = new FileOutputStream(dataFile);
			dataBos = new BufferedOutputStream(dataFos, WRITE_BUFFER_SIZE);

			ReferenceCountedFile rcIndex = null;
			if (indexFile != null)
				rcIndex = new ReferenceCountedFile(indexFile.getAbsolutePath());

			ReferenceCountedFile rcData = new ReferenceCountedFile(dataFile.getAbsolutePath());

			this.run = new Run(id, length, rcIndex, rcData);
		}
	}

	public void write(Item o) throws IOException {
		written++;
		if (run.cached != null)
			run.cached.add(o);
		else {
			ByteBuffer buf = enc.encode(o, SortCodec.instance);
			int len = buf.remaining();

			if (!noIndexWrite) {
				IoHelper.encodeLong(longbuf, dataOffset);
				indexBos.write(longbuf);
			}

			IoHelper.encodeInt(intbuf, len);
			dataBos.write(intbuf);
			dataBos.write(buf.array(), 0, len);
			buf.clear();

			dataOffset += 4 + len;
		}
	}

	public Run finish() {
		ensureClose(indexBos, indexFos);
		ensureClose(dataBos, dataFos);

		run.updateLength();
		return run;
	}

	private void ensureClose(BufferedOutputStream bos, FileOutputStream fos) {
		if (bos != null) {
			try {
				bos.close();
			} catch (IOException e) {
				logger.error("kraken logdb: cannot close run output", e);
			}
		}

		if (fos != null) {
			try {
				fos.close();
			} catch (IOException e) {
				logger.error("kraken logdb: cannot close run output", e);
			}
		}
	}
}
