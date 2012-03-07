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

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.base.RemoteLoggerFactoryInfo;
import org.krakenapps.base.SentryProxy;
import org.krakenapps.base.SentryProxyRegistry;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(BaseScript.class.getName());
	private ScriptContext context;
	private SentryProxyRegistry sentryRegistry;

	public BaseScript(SentryProxyRegistry sentryRegistry) {
		this.sentryRegistry = sentryRegistry;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "list all connected sentries")
	public void list(String[] args) {
		context.println("Connected Sentry List");
		context.println("-------------------------");

		for (String guid : sentryRegistry.getSentryGuids()) {
			SentryProxy sentry = sentryRegistry.getSentry(guid);
			context.println(sentry.toString());
		}
	}

	@ScriptUsage(description = "request log channel", arguments = { @ScriptArgument(name = "guid", type = "string", description = "the guid of sentry") })
	public void requestLogChannel(String[] args) {
		String guid = args[0];

		SentryProxy sentry = sentryRegistry.getSentry(guid);
		if (sentry == null) {
			context.println("sentry not found: " + guid);
			return;
		}

		try {
			sentry.requestLogChannel();
			context.println("requested log channel");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "create remote logger", arguments = {
			@ScriptArgument(name = "guid", type = "string", description = "the guid of sentry"),
			@ScriptArgument(name = "logger factory name", type = "string", description = "the remote logger factory name"),
			@ScriptArgument(name = "logger name", type = "string", description = "new logger name"),
			@ScriptArgument(name = "description", type = "string", description = "new logger description") })
	public void createRemoteLogger(String[] args) {
		String guid = args[0];
		String factoryName = args[1];
		String name = args[2];
		String description = args[3];

		SentryProxy sentry = sentryRegistry.getSentry(guid);
		if (sentry == null) {
			context.println("sentry not found: " + guid);
			return;
		}

		try {
			RemoteLoggerFactoryInfo info = sentry.getRemoteLoggerFactory(factoryName);
			Properties props = new Properties();
			for (LoggerConfigOption option : info.getConfigOptions()) {
				setOption(props, option);
			}

			sentry.createRemoteLogger(factoryName, name, description, props);
			context.println("logger created");
		} catch (InterruptedException e) {
			context.println("interrupted");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	private void setOption(Properties props, LoggerConfigOption option) throws InterruptedException {
		String mandatory = option.isRequired() ? "(required)" : "(optional)";
		context.print(option.getDisplayName(Locale.ENGLISH) + " " + mandatory + "? ");
		String value = context.readLine();
		if (value.isEmpty() && option.isRequired()) {
			setOption(props, option);
		}

		if (!value.isEmpty()) {
			props.put(option.getName(), value);
		}
	}

	@ScriptUsage(description = "remove remote logger", arguments = {
			@ScriptArgument(name = "guid", type = "string", description = "the guid of sentry"),
			@ScriptArgument(name = "logger name", type = "string", description = "the remote logger name") })
	public void removeRemoteLogger(String[] args) {
		String guid = args[0];
		String name = args[1];

		SentryProxy sentry = sentryRegistry.getSentry(guid);
		if (sentry == null) {
			context.println("sentry not found: " + guid);
			return;
		}

		try {
			sentry.removeRemoteLogger(name);
			context.println("logger removed");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "start remote logger", arguments = {
			@ScriptArgument(name = "guid", type = "string", description = "the guid of sentry"),
			@ScriptArgument(name = "logger name", type = "string", description = "the remote logger name"),
			@ScriptArgument(name = "interval", type = "int", description = "thread sleep interval in milliseconds") })
	public void startRemoteLogger(String[] args) {
		String guid = args[0];

		SentryProxy sentry = sentryRegistry.getSentry(guid);
		if (sentry == null) {
			context.println("sentry not found: " + guid);
			return;
		}

		try {
			String name = args[1];
			int interval = Integer.parseInt(args[2]);

			sentry.startRemoteLogger(name, interval);
			context.println("logger started");
		} catch (NumberFormatException e) {
			context.println("interval should be number");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "stop remote logger", arguments = {
			@ScriptArgument(name = "guid", type = "string", description = "the guid of sentry"),
			@ScriptArgument(name = "logger name", type = "string", description = "the remote logger name"),
			@ScriptArgument(name = "timeout", type = "string", description = "waiting timeout", optional = true) })
	public void stopRemoteLogger(String[] args) {
		String guid = args[0];

		SentryProxy sentry = sentryRegistry.getSentry(guid);
		if (sentry == null) {
			context.println("sentry not found: " + guid);
			return;
		}

		try {
			String name = args[1];
			int timeout = 5000;

			if (args.length > 2)
				timeout = Integer.parseInt(args[2]);

			sentry.stopRemoteLogger(name, timeout);
			context.println("logger stopped");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "get remote logger factories", arguments = { @ScriptArgument(name = "guid", type = "string", description = "the guid of sentry") })
	public void remoteLoggerFactories(String[] args) {
		String guid = args[0];

		SentryProxy sentry = sentryRegistry.getSentry(guid);
		if (sentry == null) {
			context.println("sentry not found: " + guid);
			return;
		}

		try {
			context.println("Remote Logger Factories");
			context.println("-------------------------");

			Map<String, RemoteLoggerFactoryInfo> factories = sentry.getRemoteLoggerFactories();
			for (RemoteLoggerFactoryInfo f : factories.values()) {
				context.println(f.toString());
			}
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("remote factories call failed", e);
		}
	}

	@ScriptUsage(description = "get remote factories", arguments = { @ScriptArgument(name = "guid", type = "string", description = "the guid of sentry") })
	public void remoteLoggers(String[] args) {
		String guid = args[0];

		SentryProxy sentry = sentryRegistry.getSentry(guid);
		if (sentry == null) {
			context.println("sentry not found: " + guid);
			return;
		}

		try {
			context.println("Remote Loggers");
			context.println("---------------------");

			Map<String, org.krakenapps.log.api.Logger> loggers = sentry.getRemoteLoggers();
			for (org.krakenapps.log.api.Logger logger : loggers.values()) {
				context.println(logger.toString());
			}
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("remote loggers call failed", e);
		}
	}

	@ScriptUsage(description = "connect remote logger", arguments = {
			@ScriptArgument(name = "guid", type = "string", description = "the guid of sentry "),
			@ScriptArgument(name = "logger name", type = "string", description = "the remote logger name") })
	public void connectRemoteLogger(String[] args) {
		String guid = args[0];
		String loggerName = args[1];

		SentryProxy sentry = sentryRegistry.getSentry(guid);
		if (sentry == null) {
			context.println("sentry not found: " + guid);
			return;
		}

		try {
			sentry.connectRemoteLogger(loggerName);
			context.println("connected remote logger");
		} catch (Exception e) {
			context.println("error: " + e.getMessage());
			logger.error("failed to connect logger", e);
		}
	}

	@ScriptUsage(description = "disconnect remote logger", arguments = {
			@ScriptArgument(name = "guid", type = "string", description = "the guid of sentry "),
			@ScriptArgument(name = "logger name", type = "string", description = "the remote logger name") })
	public void disconnectRemoteLogger(String[] args) {
		String guid = args[0];
		String loggerName = args[1];

		SentryProxy sentry = sentryRegistry.getSentry(guid);
		if (sentry == null) {
			context.println("sentry not found: " + guid);
			return;
		}

		try {
			sentry.disconnectRemoteLogger(loggerName);
			context.println("disconnected remote logger");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "call sentry method", arguments = {
			@ScriptArgument(name = "guid", type = "string", description = "the guid of sentry "),
			@ScriptArgument(name = "method", type = "string", description = "the rpc method name"),
			@ScriptArgument(name = "arguments", type = "string", description = "string arguments", optional = true) })
	public void call(String[] args) {
		String guid = args[0];
		String method = args[1];
		String[] params = null;
		if (args.length > 2)
			params = Arrays.copyOfRange(args, 2, args.length);

		SentryProxy sentry = sentryRegistry.getSentry(guid);
		if (sentry == null) {
			context.println("sentry not found: " + guid);
			return;
		}

		try {
			Object value = sentry.call(method, params);
			if (value != null)
				pp(0, value);
		} catch (RpcException e) {
			context.println("rpc error: " + e.getMessage());
		} catch (InterruptedException e) {
			context.println("interrupted");
		}
	}

	private void pp(int depth, Object value) {
		pp(depth, value, false);
	}

	@SuppressWarnings("unchecked")
	private void pp(int depth, Object value, boolean ignoreDepth) {
		String prefix = "";
		if (!ignoreDepth) {
			for (int i = 0; i < depth; i++)
				prefix += " ";
		}

		if (value == null) {
			context.println(prefix + "null");
		} else if (value instanceof Map<?, ?>) {
			Map<String, Object> m = (Map<String, Object>) value;
			context.println(prefix + "{");
			for (String key : m.keySet()) {
				context.print(prefix + "  \"" + key + "\": ");
				pp(depth + 1, m.get(key), true);
			}
			context.println(prefix + "}");
		} else if (value.getClass().isArray()) {
			if (value.getClass().getName().equals("[B")) {
				byte[] b = (byte[]) value;
				context.println(prefix + "[");
				for (int i = 0; i < b.length; i++)
					context.print(String.format("0x%2x ", b[i]));

				context.println(prefix + "]");

			} else {
				Object[] array = (Object[]) value;
				context.println(prefix + "[");
				for (int i = 0; i < array.length; i++)
					pp(depth + 1, array[i]);

				context.println(prefix + "]");
			}
		} else {
			context.println(prefix + value);
		}

	}
}
