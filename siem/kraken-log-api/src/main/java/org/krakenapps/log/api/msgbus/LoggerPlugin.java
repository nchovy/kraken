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
package org.krakenapps.log.api.msgbus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.log.api.LogNormalizerFactoryRegistry;
import org.krakenapps.log.api.LogParserFactory;
import org.krakenapps.log.api.LogParserFactoryRegistry;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerFactoryRegistry;
import org.krakenapps.log.api.LoggerRegistry;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "logapi-logger-plugin")
@MsgbusPlugin
public class LoggerPlugin {
	@Requires
	private LoggerRegistry loggerRegistry;

	@Requires
	private LoggerFactoryRegistry loggerFactoryRegistry;

	@Requires
	private LogNormalizerFactoryRegistry normalizerRegistry;

	@Requires
	private LogParserFactoryRegistry parserFactoryRegistry;

	@MsgbusMethod
	public void getLoggerFactories(Request req, Response resp) {
		Locale locale = req.getSession().getLocale();
		resp.put("factories", Marshaler.marshal(loggerFactoryRegistry.getLoggerFactories(), locale));
	}

	@MsgbusMethod
	public void getParserFactories(Request req, Response resp) {
		Locale locale = req.getSession().getLocale();
		List<Object> l = new ArrayList<Object>();

		for (String name : parserFactoryRegistry.getNames()) {
			LogParserFactory f = parserFactoryRegistry.get(name);
			l.add(Marshaler.marshal(f, locale));
		}

		resp.put("factories", l);
	}

	@MsgbusMethod
	public void getLoggers(Request req, Response resp) {
		resp.put("loggers", Marshaler.marshal(loggerRegistry.getLoggers()));
	}

	@MsgbusMethod
	public void getLogger(Request req, Response resp) {
		// logger fullname
		Logger logger = loggerRegistry.getLogger(req.getString("logger"));
		if (logger != null)
			resp.put("logger", Marshaler.marshal(logger));
		else
			resp.put("logger", null);
	}

	@MsgbusMethod
	public void getNormalizers(Request req, Response resp) {
		resp.put("normalizers", normalizerRegistry.getNames());
	}

	@MsgbusMethod
	public void getFactoryOptions(Request req, Response resp) {
		String loggerFactoryName = req.getString("factory");
		Locale locale = req.getSession().getLocale();
		LoggerFactory loggerFactory = loggerFactoryRegistry.getLoggerFactory(loggerFactoryName);
		resp.put("options", Marshaler.marshal(loggerFactory.getConfigOptions(), locale));
	}

	/**
	 * ensure updated logger configuration, and running state
	 */
	@MsgbusMethod
	public void ensureLoggerOperation(Request req, Response resp) {
		removeLogger(req, resp);
		createLogger(req, resp);
		startLogger(req, resp);
	}

	@MsgbusMethod
	public void createLogger(Request req, Response resp) {
		String loggerFactoryName = req.getString("factory");
		String loggerNamespace = req.getString("namespace");
		String loggerName = req.getString("name");
		String description = req.getString("description");

		if (req.has("logger")) {
			String loggerFullname = req.getString("logger");
			int pos = loggerFullname.indexOf('\\');
			if (pos < 0) {
				loggerNamespace = "local";
				loggerName = loggerFullname;
			} else {
				loggerNamespace = loggerFullname.substring(0, pos);
				loggerName = loggerFullname.substring(pos + 1);
			}
		}

		LoggerFactory loggerFactory = loggerFactoryRegistry.getLoggerFactory(loggerFactoryName);
		Properties config = new Properties();

		for (LoggerConfigOption opt : loggerFactory.getConfigOptions()) {
			String value = null;

			try {
				value = req.getString(opt.getName());
				config.put(opt.getName(), value);
			} catch (NullPointerException e) {
				if (opt.isRequired())
					throw e;
			}
		}

		loggerFactory.newLogger(loggerNamespace, loggerName, description, config);
	}

	@MsgbusMethod
	public void removeLoggers(Request req, Response resp) {
		@SuppressWarnings("unchecked")
		List<String> loggers = (List<String>) req.get("loggers");

		for (String fullName : loggers) {
			Logger logger = loggerRegistry.getLogger(fullName);
			if (logger == null)
				continue;

			logger.stop();
			String[] tokens = fullName.split("\\\\");
			LoggerFactory factory = loggerFactoryRegistry.getLoggerFactory(logger.getFactoryNamespace(), logger.getFactoryName());
			factory.deleteLogger(tokens[0], tokens[1]);
		}
	}

	@MsgbusMethod
	public void removeLogger(Request req, Response resp) {
		String fullName = req.getString("logger");

		Logger logger = loggerRegistry.getLogger(fullName);
		if (logger == null)
			return;

		logger.stop();
		String[] tokens = fullName.split("\\\\");
		LoggerFactory factory = loggerFactoryRegistry.getLoggerFactory(logger.getFactoryNamespace(), logger.getFactoryName());
		factory.deleteLogger(tokens[0], tokens[1]);
	}

	@MsgbusMethod
	public void startLogger(Request req, Response resp) {
		String fullName = req.getString("logger");
		int interval = req.getInteger("interval");
		Logger logger = loggerRegistry.getLogger(fullName);
		if (logger == null)
			return;

		logger.start(interval);
	}

	@MsgbusMethod
	public void stopLogger(Request req, Response resp) {
		String fullName = req.getString("logger");
		int waitTime = 5000;
		if (req.has("wait_time"))
			waitTime = req.getInteger("wait_time");

		Logger logger = loggerRegistry.getLogger(fullName);
		if (logger == null)
			return;

		logger.stop(waitTime);
	}

}
