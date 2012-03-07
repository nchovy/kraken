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

package org.krakenapps.logger;

import org.apache.felix.framework.Logger;
import org.krakenapps.api.LoggerControlService;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.KrakenLoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

public class KrakenLogService implements LogService, LoggerControlService {
	private KrakenLoggerFactory loggerFactory = (KrakenLoggerFactory) StaticLoggerBinder.getSingleton()
			.getLoggerFactory();

	@Override
	public boolean hasLogger(String name) {
		return loggerFactory.hasLogger(name);
	}

	@Override
	public void setLogLevel(String name, String level, boolean isEnabled) {
		loggerFactory.setLogLevel(name, level, isEnabled);
	}

	@Override
	public void log(int level, String message, Throwable exception) {
		Logger logger = (Logger) LoggerFactory.getLogger(KrakenLogService.class.getName());
		logger.log(level, message, exception);
	}

	@Override
	public void log(int level, String message) {
		Logger logger = (Logger) LoggerFactory.getLogger(KrakenLogService.class.getName());
		logger.log(level, message);
	}

	@Override
	public void log(ServiceReference sr, int level, String message, Throwable exception) {
		Logger logger = (Logger) LoggerFactory.getLogger(KrakenLogService.class.getName());
		logger.log(level, message, exception);
	}

	@Override
	public void log(ServiceReference sr, int level, String message) {
		Logger logger = (Logger) LoggerFactory.getLogger(KrakenLogService.class.getName());
		logger.log(level, message);
	}

}
