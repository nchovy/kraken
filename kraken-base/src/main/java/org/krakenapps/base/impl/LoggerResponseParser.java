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
package org.krakenapps.base.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.krakenapps.base.RemoteLogger;
import org.krakenapps.base.SentryProxy;
import org.krakenapps.log.api.Logger;

public class LoggerResponseParser {
	private LoggerResponseParser() {
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Logger> parse(SentryProxy proxy, Object[] loggers) {
		Map<String, Logger> m = new HashMap<String, Logger>();
		for (Object logger : loggers) {
			Logger l = parse(proxy, (Map<String, Object>) logger);
			m.put(l.getName(), l);
		}
		return m;
	}

	public static RemoteLogger parse(SentryProxy proxy, Map<String, Object> logger) {
		String factoryName = (String) logger.get("factory_name");
		String loggerName = (String) logger.get("logger_name");
		String description = (String) logger.get("description");
		boolean isRunning = (Boolean) logger.get("is_running");
		int interval = (Integer) logger.get("interval");

		RemoteLogger remoteLogger = new RemoteLogger(proxy, loggerName, factoryName, description, new Properties());
		remoteLogger.setRunning(isRunning);
		remoteLogger.setInterval(interval);
		return remoteLogger;
	}
}
