/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logdb.query.parser;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.krakenapps.log.api.LogParser;
import org.krakenapps.log.api.LogParserFactory;
import org.krakenapps.log.api.LogParserFactoryRegistry;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryCommandParser;
import org.krakenapps.logdb.LogQueryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipFileParser implements LogQueryCommandParser {
	private final Logger logger = LoggerFactory.getLogger(ZipFileParser.class);
	private LogParserFactoryRegistry parserFactoryRegistry;

	public ZipFileParser(LogParserFactoryRegistry parserFactoryRegistry) {
		this.parserFactoryRegistry = parserFactoryRegistry;
	}

	@Override
	public String getCommandName() {
		return "zipfile";
	}

	@Override
	public LogQueryCommand parse(LogQueryContext context, String commandString) {
		try {
			QueryTokens tokens = QueryTokenizer.tokenize(commandString);
			Map<String, String> options = tokens.options();

			String filePath = tokens.reverseArg(1);
			String entryPath = tokens.lastArg();

			int offset = 0;
			if (options.containsKey("offset"))
				offset = Integer.valueOf(options.get("offset"));

			int limit = 0;
			if (options.containsKey("limit"))
				limit = Integer.valueOf(options.get("limit"));

			File file = new File(filePath);
			if (!file.exists())
				throw new IllegalStateException("zipfile [" + file.getAbsolutePath() + "] not found");

			if (!file.canRead())
				throw new IllegalStateException("cannot read zipfile [" + file.getAbsolutePath() + "], check read permission");

			ZipFile zipFile = new ZipFile(file);
			logger.debug("kraken logdb: zipfile path: {}, zip entry: {}", filePath, entryPath);

			ZipEntry entry = zipFile.getEntry(entryPath);
			if (entry == null)
				throw new IllegalStateException("entry [" + entryPath + "] not found in zip file [" + filePath + "]");

			InputStream is = zipFile.getInputStream(entry);
			String parserName = options.get("parser");
			LogParser parser = null;
			if (parserName != null) {
				LogParserFactory factory = parserFactoryRegistry.get(parserName);
				if (factory == null)
					throw new IllegalStateException("log parser not found: " + parserName);

				parser = factory.createParser(convert(options));
			}

			return new org.krakenapps.logdb.query.command.ZipFile(is, parser, offset, limit);
		} catch (Throwable t) {
			throw new RuntimeException("cannot create zipfile source", t);
		}
	}

	private Properties convert(Map<String, String> options) {
		Properties p = new Properties();
		for (String key : options.keySet()) {
			String value = options.get(key);
			if (value != null)
				p.put(key, value);
		}

		return p;
	}
}
