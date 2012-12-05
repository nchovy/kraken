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
package org.krakenapps.log.api.impl;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.log.api.Log;
import org.krakenapps.log.api.LogNormalizer;
import org.krakenapps.log.api.LogNormalizerFactory;
import org.krakenapps.log.api.LogNormalizerFactoryRegistry;
import org.krakenapps.log.api.LogParserFactoryRegistry;
import org.krakenapps.log.api.LogPipe;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerFactoryRegistry;
import org.krakenapps.log.api.LoggerRegistry;

public class LogApiScript implements Script {
	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(LogApiScript.class.getName());
	private ScriptContext context;
	private LoggerFactoryRegistry loggerFactoryRegistry;
	private LoggerRegistry loggerRegistry;
	private LogParserFactoryRegistry parserFactoryRegistry;
	private LogNormalizerFactoryRegistry normalizerRegistry;

	public LogApiScript(LoggerFactoryRegistry loggerFactoryRegistry, LoggerRegistry loggerRegistry,
			LogParserFactoryRegistry parserFactoryRegistry, LogNormalizerFactoryRegistry normalizerRegistry) {
		this.loggerFactoryRegistry = loggerFactoryRegistry;
		this.loggerRegistry = loggerRegistry;
		this.parserFactoryRegistry = parserFactoryRegistry;
		this.normalizerRegistry = normalizerRegistry;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void normalizers(String[] args) {
		context.println("Log Normalizers");
		context.println("---------------------");

		for (String name : normalizerRegistry.getNames()) {
			context.println(name);
		}
	}

	public void normalize(String[] args) {
		try {
			context.print("Normalizer Name? ");
			String normalizerName = context.readLine();
			LogNormalizerFactory factory = normalizerRegistry.get(normalizerName);
			if (factory == null) {
				context.println("normalizer not found");
				return;
			}

			LogNormalizer normalizer = factory.createNormalizer(new Properties());

			Map<String, Object> params = getParams();
			Map<String, Object> m = normalizer.normalize(params);
			context.println("---------------------");
			for (String key : m.keySet()) {
				context.println(key + ": " + m.get(key));
			}
		} catch (InterruptedException e) {
			context.println("");
			context.println("interrupted");
		}
	}

	public void loggerFactories(String[] args) {
		context.println("Logger Factories");
		context.println("---------------------");

		for (LoggerFactory loggerFactory : loggerFactoryRegistry.getLoggerFactories()) {
			context.println(loggerFactory.toString());
		}
	}

	public void loggers(String[] args) {
		context.println("Loggers");
		context.println("----------------------");

		for (Logger logger : loggerRegistry.getLoggers()) {
			context.println(logger.toString());
		}
	}

	@ScriptUsage(description = "print logger configuration", arguments = { @ScriptArgument(name = "logger fullname", type = "string", description = "logger fullname") })
	public void logger(String[] args) {
		String fullName = args[0];
		context.println("Logger [" + fullName + "]");
		printLine(fullName.length() + 10);
		Logger logger = loggerRegistry.getLogger(fullName);
		if (logger == null) {
			context.println("logger not found");
			return;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String lastLogDate = logger.getLastLogDate() != null ? dateFormat.format(logger.getLastLogDate()) : "N/A";
		String lastRunDate = logger.getLastRunDate() != null ? dateFormat.format(logger.getLastRunDate()) : "N/A";

		context.println(" * Description: " + logger.getDescription());
		context.println(" * Logger Factory: " + logger.getFactoryFullName());
		context.println(" * Status: " + logger.getStatus());
		context.println(" * Interval: " + logger.getInterval() + "ms");
		context.println(" * Last Log: " + lastLogDate);
		context.println(" * Last Run: " + lastRunDate);
		context.println(" * Log Count: " + logger.getLogCount());
		context.println("");

		context.println("Configuration");
		context.println("---------------");
		Properties props = logger.getConfig();
		if (props != null) {
			for (Object key : props.keySet())
				context.println(" * " + key + ": " + props.get(key));
		}
	}

	private void printLine(int len) {
		for (int i = 0; i < len; i++)
			context.print('-');
		context.println();
	}

	public void parserFactories(String[] args) {
		context.println("Log Parser Factories");
		context.println("----------------------");

		for (String name : parserFactoryRegistry.getNames()) {
			context.println(name);
		}
	}

	@ScriptUsage(description = "trace logger output", arguments = { @ScriptArgument(name = "logger name", type = "string", description = "logger fullname") })
	public void trace(String[] args) {
		Logger logger = loggerRegistry.getLogger(args[0]);
		ConsoleLogPipe p = new ConsoleLogPipe();
		logger.addLogPipe(p);

		try {
			context.println("tracing logger: " + logger);
			while (true) {
				context.readLine();
			}
		} catch (InterruptedException e) {
			context.println("interrupted");
		} finally {
			logger.removeLogPipe(p);
		}
	}

	private class ConsoleLogPipe implements LogPipe {
		@Override
		public void onLog(Logger logger, Log log) {
			context.println(logger.getFullName() + ": " + log.toString());
		}
	}

	@ScriptUsage(description = "start the logger", arguments = {
			@ScriptArgument(name = "logger fullname", type = "string", description = "the logger fullname to start"),
			@ScriptArgument(name = "interval", type = "int", description = "sleep time of active logger thread in milliseconds. 60000ms by default. passive logger will ignore interval", optional = true) })
	public void startLogger(String[] args) {
		try {
			String fullName = args[0];
			int interval = 60000;
			if (args.length > 1)
				interval = Integer.parseInt(args[1]);

			Logger logger = loggerRegistry.getLogger(fullName);
			if (logger == null) {
				context.println("logger not found");
				return;
			}

			if (logger.isPassive())
				logger.start();
			else
				logger.start(interval);
			context.println("logger started");
		} catch (NumberFormatException e) {
			context.println("interval should be number in milliseconds");
		} catch (IllegalStateException e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "stop the logger", arguments = {
			@ScriptArgument(name = "logger name", type = "string", description = "the logger name to stop"),
			@ScriptArgument(name = "max wait time", type = "int", description = "max wait time in milliseconds", optional = true) })
	public void stopLogger(String[] args) {
		try {
			int maxWaitTime = 5000;
			String name = args[0];
			if (args.length > 1)
				maxWaitTime = Integer.parseInt(args[1]);

			Logger logger = loggerRegistry.getLogger(name);
			if (logger == null) {
				context.println("logger not found");
				return;
			}

			if (!logger.isPassive())
				context.println("waiting...");
			
			logger.stop(maxWaitTime);
			context.println("logger stopped");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "create new logger", arguments = {
			@ScriptArgument(name = "logger factory name", type = "string", description = "logger factory name. try logapi.loggerFactories command."),
			@ScriptArgument(name = "logger namespace", type = "string", description = "new logger namespace"),
			@ScriptArgument(name = "logger name", type = "string", description = "new logger name"),
			@ScriptArgument(name = "description", type = "string", description = "the description of new logger", optional = true) })
	public void createLogger(String[] args) {
		try {
			String loggerFactoryName = args[0];
			String loggerNamespace = args[1];
			String loggerName = args[2];
			String description = (args.length > 3) ? args[3] : null;

			LoggerFactory loggerFactory = loggerFactoryRegistry.getLoggerFactory(loggerFactoryName);
			if (loggerFactory == null) {
				context.println("logger factory not found: " + loggerFactoryName);
				return;
			}

			Properties config = new Properties();
			for (LoggerConfigOption type : loggerFactory.getConfigOptions()) {
				setOption(config, type);
			}

			Logger logger = loggerFactory.newLogger(loggerNamespace, loggerName, description, config);
			if (logger == null) {
				context.println("failed to create logger");
				return;
			}

			context.println("logger created: " + logger.toString());
		} catch (InterruptedException e) {
			context.println("interrupted");
		} catch (Exception e) {
			context.println(e.getMessage());
			slog.error("kraken log api: cannot create logger", e);
		}
	}

	@ScriptUsage(description = "remove logger", arguments = { @ScriptArgument(name = "logger fullname", type = "string", description = "the logger fullname") })
	public void removeLogger(String[] args) {
		try {
			String fullName = args[0];
			Logger logger = loggerRegistry.getLogger(fullName);

			if (logger == null) {
				context.println("logger not found");
				return;
			}

			// stop logger
			logger.stop();

			String[] tokens = fullName.split("\\\\");

			LoggerFactory factory = loggerFactoryRegistry.getLoggerFactory(logger.getFactoryNamespace(), logger.getFactoryName());
			factory.deleteLogger(tokens[0], tokens[1]);
			context.println("logger removed");
		} catch (Exception e) {
			context.println("error: " + e.getMessage());
			slog.error("kraken log api: cannot remove logger", e);
		}
	}

	private void setOption(Properties config, LoggerConfigOption type) throws InterruptedException {
		String directive = type.isRequired() ? "(required)" : "(optional)";
		context.print(type.getDisplayName(Locale.ENGLISH) + " " + directive + "? ");
		String value = context.readLine();
		if (!value.isEmpty())
			config.put(type.getName(), value);

		if (value.isEmpty() && type.isRequired()) {
			setOption(config, type);
		}
	}

	private Map<String, Object> getParams() throws InterruptedException {
		Map<String, Object> params = new HashMap<String, Object>();

		while (true) {
			context.print("Key (press enter to end): ");
			String key = context.readLine();
			if (key == null || key.isEmpty())
				break;

			context.print("Value: ");
			String value = context.readLine();

			params.put(key, value);
		}
		return params;
	}
}
