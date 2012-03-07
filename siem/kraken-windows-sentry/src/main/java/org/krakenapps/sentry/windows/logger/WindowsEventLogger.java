/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.sentry.windows.logger;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.krakenapps.log.api.AbstractLogger;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerSpecification;
import org.krakenapps.log.api.LoggerStatus;
import org.krakenapps.log.api.SimpleLog;
import org.krakenapps.winapi.EventLog;
import org.krakenapps.winapi.EventLogReader;

public class WindowsEventLogger extends AbstractLogger {
	private Properties config;

	public WindowsEventLogger(LoggerSpecification spec, LoggerFactory loggerFactory) {
		super(spec.getNamespace(), spec.getName(), spec.getDescription(), loggerFactory);
		this.config = spec.getConfig();
	}

	@Override
	protected void runOnce() {
		String eventSource = config.getProperty("event_source");

		EventLogReader logReader = new EventLogReader(eventSource);

		int lastId = 0;
		if (config.getProperty("last_id") != null)
			lastId = Integer.valueOf(config.getProperty("last_id"));
		List<EventLog> logs = logReader.readAllEventLogs(lastId + 1);

		if (logs.size() == 0)
			return;

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		int lastRecordNumber = logs.get(0).getRecordNumber() - 1;
		for (EventLog log : logs) {
			if (getStatus() == LoggerStatus.Stopped || getStatus() == LoggerStatus.Stopping)
				break;

			Map<String, Object> m = new HashMap<String, Object>();
			m.put("record_number", log.getRecordNumber());
			m.put("event_id", log.getEventId());
			m.put("event_type", log.getEventType().toString());
			m.put("generated", dateFormat.format(log.getGenerated()));
			m.put("written", dateFormat.format(log.getWritten()));
			m.put("provider_name", log.getProviderName());
			m.put("event_category", log.getEventCategory());
			m.put("user", log.getUser());

			write(new SimpleLog(log.getGenerated(), getFullName(), "system", log.getMessage(), m));
			lastRecordNumber = log.getRecordNumber();
		}
		config.setProperty("last_id", Integer.toString(lastRecordNumber));
	}
}
