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
package org.krakenapps.logdb.httpinput.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.log.api.AbstractLoggerFactory;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.log.api.LoggerSpecification;
import org.krakenapps.log.api.StringConfigType;
import org.krakenapps.logdb.httpinput.HttpInputLogger;
import org.krakenapps.logdb.httpinput.HttpInputService;

@Component(name = "logdb-httpinput-logger-factory")
@Provides
public class HttpInputLoggerFactory extends AbstractLoggerFactory implements HttpInputService {
	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(HttpInputLoggerFactory.class);

	/**
	 * full name to http input logger mappings
	 */
	private ConcurrentHashMap<String, HttpInputLogger> loggers = new ConcurrentHashMap<String, HttpInputLogger>();

	/**
	 * input token to http input logger mappings
	 */
	private ConcurrentHashMap<String, HttpInputLogger> tokenToLoggers = new ConcurrentHashMap<String, HttpInputLogger>();

	@Override
	public String getName() {
		return "http-input";
	}

	@Override
	public String getDisplayName(Locale locale) {
		return "http input";
	}

	@Override
	public String getDescription(Locale locale) {
		return "receives json logs through http post requests";
	}

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		List<LoggerConfigOption> configs = new ArrayList<LoggerConfigOption>();
		configs.add(new StringConfigType("token", locales("Input Token"), locales("Submission token for authentication"), true));
		return configs;
	}

	private Map<Locale, String> locales(String text) {
		Map<Locale, String> m = new HashMap<Locale, String>();
		m.put(Locale.ENGLISH, text);
		return m;
	}

	@Override
	protected Logger createLogger(LoggerSpecification spec) {
		HttpInputLogger logger = new HttpInputLogger(this, spec);
		loggers.put(logger.getFullName(), logger);
		tokenToLoggers.put(logger.getToken(), logger);
		slog.debug("kraken-logdb-httpinput: created logger [{}]", logger);
		return logger;
	}

	@Override
	public void deleteLogger(String namespace, String name) {
		HttpInputLogger logger = loggers.remove(namespace + "\\" + name);
		if (logger != null) {
			tokenToLoggers.remove(logger.getToken());
			slog.debug("kraken-logdb-httpinput: removed logger [{}]", logger);
		}

		super.deleteLogger(namespace, name);
	}

	@Override
	public List<HttpInputLogger> getLoggers() {
		return new ArrayList<HttpInputLogger>(loggers.values());
	}

	@Override
	public HttpInputLogger findLogger(String token) {
		return tokenToLoggers.get(token);
	}

}
