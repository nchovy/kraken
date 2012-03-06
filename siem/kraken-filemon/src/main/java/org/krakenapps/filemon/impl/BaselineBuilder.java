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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.krakenapps.codec.EncodingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaselineBuilder {
	private final Logger logger = LoggerFactory.getLogger(BaselineBuilder.class.getName());
	private final File baseDir;
	private Set<File> files;
	private Set<File> paths;
	private Set<Pattern> excludes;

	public BaselineBuilder(Set<File> paths, Set<Pattern> excludes) {
		this.baseDir = getBaseDirectory();
		this.files = new TreeSet<File>();
		this.paths = paths;
		this.excludes = excludes;

		for (File f : paths)
			scan(f, excludes);

		logger.trace("kraken filemon: set base directory [{}]", baseDir.getAbsolutePath());
	}

	private void scan(File f, Set<Pattern> excludes) {
		if (f.isFile() && !isExcluded(excludes, f))
			files.add(f);

		if (f.isDirectory()) {
			for (File entry : f.listFiles())
				scan(entry, excludes);
		}
	}

	private boolean isExcluded(Set<Pattern> excludes, File f) {
		String path = f.getAbsolutePath();
		for (Pattern p : excludes)
			if (p.matcher(path).matches())
				return true;

		return false;
	}

	public Set<File> getTargetFiles() {
		return files;
	}

	public File build() throws IOException {
		File db = File.createTempFile("kraken-filemon-", ".db");
		FileOutputStream os = new FileOutputStream(db);
		try {
			// write file headers
			Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("created", new Date());
			headers.put("file_count", files.size());
			headers.put("includes", marshalPaths());
			headers.put("excludes", marshalExcludes());

			int len = EncodingRule.lengthOf(headers);
			ByteBuffer b = ByteBuffer.allocate(len);
			EncodingRule.encode(b, headers);
			os.write(b.array());

			// write files
			ByteBuffer bb = ByteBuffer.allocate(512);
			for (File f : files) {
				long fileSize = f.length();
				long lastModified = f.lastModified();
				boolean hidden = f.isHidden();
				String hash = HashUtils.getSha1(f);

				Object[] record = new Object[] { f.getAbsolutePath(), fileSize, lastModified, hidden, hash };
				EncodingRule.encode(bb, record);
				bb.flip();
				os.write(bb.array(), 0, bb.limit());
				bb.clear();

				logger.trace("kraken filemon: adding file [{}] to baseline", f.getAbsolutePath());
			}
			return db;
		} finally {
			logger.trace("kraken filemon: baseline [{}] created", db.getAbsolutePath());
			os.close();

			File target = new File(baseDir, "kraken-filemon-baseline.db");
			boolean deleted = target.delete();

			if (!deleted)
				logger.trace("kraken filemon: old baseline not removed");

			db.renameTo(target);
			logger.trace("kraken filemon: created baseline [{}]", target.getAbsolutePath());
		}
	}

	private List<String> marshalPaths() {
		List<String> l = new LinkedList<String>();
		for (File f : paths)
			l.add(f.getAbsolutePath());
		return l;
	}

	private List<String> marshalExcludes() {
		List<String> l = new LinkedList<String>();
		for (Pattern p : excludes)
			l.add(p.pattern());
		return l;
	}

	private File getBaseDirectory() {
		File dir = new File(System.getProperty("kraken.data.dir"), "kraken-filemon/");
		dir.mkdirs();
		return dir;
	}
}
