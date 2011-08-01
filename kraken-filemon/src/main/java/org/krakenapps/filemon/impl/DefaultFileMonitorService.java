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
package org.krakenapps.filemon.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.filemon.FileMonitorEventListener;
import org.krakenapps.filemon.FileMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "file-monitor-service")
@Provides
public class DefaultFileMonitorService implements FileMonitorService {
	private final Logger logger = LoggerFactory.getLogger(DefaultFileMonitorService.class.getName());
	private final File baseDir;
	private Set<File> inclusionPaths;
	private Set<Pattern> excludePatterns;
	private CopyOnWriteArraySet<FileMonitorEventListener> callbacks;

	private Date created = null;
	private Integer fileCount;

	public DefaultFileMonitorService() {
		this.baseDir = getBaseDirectory();
		this.inclusionPaths = Collections.synchronizedSet(new TreeSet<File>());
		this.excludePatterns = Collections.synchronizedSet(new TreeSet<Pattern>());
		this.callbacks = new CopyOnWriteArraySet<FileMonitorEventListener>();
	}

	@Override
	public String getMd5(File f) throws IOException {
		return HashUtils.getMd5(f);
	}

	@Override
	public String getSha1(File f) throws IOException {
		return HashUtils.getSha1(f);
	}

	@Override
	public Integer getLastFileCount() {
		return fileCount;
	}

	@Override
	public Date getLastTimestamp() {
		return created;
	}

	@Override
	public void createBaseline() throws IOException {
		BaselineBuilder builder = new BaselineBuilder(inclusionPaths, excludePatterns);
		builder.build();
	}

	@Validate
	public void start() {
		try {
			Map<String, Object> headers = readMetadata();
			created = (Date) headers.get("created");
			fileCount = (Integer) headers.get("file_count");

			Object[] inclusions = (Object[]) headers.get("includes");
			for (Object inclusion : inclusions) {
				inclusionPaths.add(new File((String) inclusion));
			}

			Object[] patterns = (Object[]) headers.get("excludes");
			for (Object exclusion : patterns) {
				excludePatterns.add(Pattern.compile((String) exclusion));
			}

		} catch (IOException e) {
			logger.error("kraken filemon: cannot open baseline db", e);
		}
	}

	private Map<String, Object> readMetadata() throws IOException {
		File db = new File(baseDir, "kraken-filemon-baseline.db");

		FileInputStream is = null;
		try {
			is = new FileInputStream(db);
			return readHeaders(is);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
	}

	@Override
	public void check() {
		File db = new File(baseDir, "kraken-filemon-baseline.db");

		FileInputStream is = null;
		try {
			is = new FileInputStream(db);
			readHeaders(is);

			while (true) {
				if (is.available() <= 0)
					break;

				ByteBuffer bb = readNext(is);
				Object[] record = (Object[]) EncodingRule.decode(bb);
				if (record == null)
					break;

				Baseline baseline = new Baseline(new File((String) record[0]), (Long) record[1], (Long) record[2],
						(Boolean) record[3], (String) record[4]);

				fireCheckCallback(baseline);

				if (baseline.isDeleted()) {
					fireDeleteCallback(baseline);
				} else if (baseline.isModified()) {
					fireModifiedCallback(baseline);
				}
			}
		} catch (IOException e) {
			logger.error("kraken filemon: cannot open baseline db", e);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
	}

	private void fireCheckCallback(Baseline baseline) {
		for (FileMonitorEventListener callback : callbacks) {
			try {
				callback.onCheck(baseline.getFile());
			} catch (Exception e) {
				logger.warn("kraken filemon: file monitor callback should not throw any exception", e);
			}
		}
	}

	private void fireModifiedCallback(Baseline baseline) throws IOException {
		logger.trace("kraken filemon: file modified, %s", baseline.getFileChange());

		for (FileMonitorEventListener callback : callbacks) {
			try {
				callback.onModified(baseline.getFileChange());
			} catch (Exception e) {
				logger.warn("kraken filemon: file monitor callback should not throw any exception", e);
			}
		}
	}

	private void fireDeleteCallback(Baseline baseline) {
		for (FileMonitorEventListener callback : callbacks) {
			try {
				callback.onDeleted(baseline.getFile());
			} catch (Exception e) {
				logger.warn("kraken filemon: file monitor callback should not throw any exception", e);
			}
		}
	}

	private Map<String, Object> readHeaders(FileInputStream is) throws IOException {
		ByteBuffer bb = readNext(is);
		return EncodingRule.decodeMap(bb);
	}

	private ByteBuffer readNext(FileInputStream is) throws IOException {
		byte[] b = new byte[5];
		ByteBuffer bb = ByteBuffer.wrap(b);
		int type = is.read();

		int i = 0;
		for (;;) {
			b[i] = (byte) is.read();
			if ((b[i] & 0x80) == 0)
				break;
			i++;
		}

		int payloadLen = (int) EncodingRule.decodeRawNumber(bb);
		int headerLength = 1 + EncodingRule.lengthOfRawNumber(int.class, payloadLen);

		int totalLength = headerLength + payloadLen;
		b = new byte[totalLength];
		bb = ByteBuffer.wrap(b);

		// rebuild bytes in memory
		bb.put((byte) type);
		EncodingRule.encodeNumber(bb, Long.class, payloadLen);
		is.read(b, headerLength, payloadLen);
		bb.flip();
		bb.limit(totalLength);
		return bb;
	}

	@Override
	public Collection<File> getInclusionPaths() {
		return inclusionPaths;
	}

	@Override
	public Collection<Pattern> getExclusionPatterns() {
		return excludePatterns;
	}

	@Override
	public void addInclusionPath(File f) {
		inclusionPaths.add(f);
	}

	@Override
	public void removeInclusionPath(File f) {
		inclusionPaths.remove(f);
	}

	@Override
	public void addExclusionPattern(String regex) {
		excludePatterns.add(Pattern.compile(regex));
	}

	@Override
	public void removeExclusionPattern(String regex) {
		excludePatterns.remove(Pattern.compile(regex));
	}

	@Override
	public void addEventListener(FileMonitorEventListener callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeEventListener(FileMonitorEventListener callback) {
		callbacks.remove(callback);
	}

	public static void main(String[] args) {
		new DefaultFileMonitorService().check();
	}

	private File getBaseDirectory() {
		File dir = new File(System.getProperty("kraken.data.dir"), "kraken-filemon/");
		dir.mkdirs();
		return dir;
	}

}
