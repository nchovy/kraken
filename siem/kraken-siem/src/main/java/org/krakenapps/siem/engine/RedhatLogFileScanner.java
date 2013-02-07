/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.siem.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.siem.CandidateTextFileLogger;
import org.krakenapps.siem.LogFileScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "siem-redhat-logfile-scanner")
@Provides
public class RedhatLogFileScanner implements LogFileScanner {
	private final Logger slog = LoggerFactory.getLogger(RedhatLogFileScanner.class.getName());

	@Override
	public String getName() {
		return "redhat";
	}

	@Override
	public Collection<CandidateTextFileLogger> scan() {
		List<CandidateTextFileLogger> loggers = new ArrayList<CandidateTextFileLogger>();

		// openssh log file
		slog.trace("kraken siem: scanning openssh file");
		File file = new File("/var/log/secure");

		if (file.isFile() && file.canRead()) {
			CandidateTextFileLogger logger = new CandidateTextFileLogger();
			logger.setName("openssh");
			logger.setFile(file);
			logger.setFileEncoding("utf-8");
			logger.getMetadata().put("date.pattern", "MMM dd HH:mm:ss");
			logger.getMetadata().put("date.locale", "en");
			logger.getMetadata().put("logparser", "openssh");

			loggers.add(logger);
		}

		return loggers;
	}

	@Override
	public String toString() {
		return "redhat log file scanner";
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", getName());
		m.put("description", toString());
		return m;
	}
}
