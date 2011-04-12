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
package org.krakenapps.siem.script;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.event.api.Event;
import org.krakenapps.event.api.EventDispatcher;
import org.krakenapps.event.api.EventSeverity;
import org.krakenapps.jpa.JpaService;
import org.krakenapps.log.api.LoggerFactoryRegistry;
import org.krakenapps.log.api.LoggerRegistry;
import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogSearchCallback;
import org.krakenapps.siem.CandidateTextFileLogger;
import org.krakenapps.siem.EventServer;
import org.krakenapps.siem.LogFileScanner;
import org.krakenapps.siem.LogFileScannerRegistry;
import org.krakenapps.siem.LogServer;
import org.krakenapps.siem.engine.EventResponseMapper;
import org.krakenapps.siem.engine.IscHttpRuleManager;
import org.krakenapps.siem.engine.ResponseKey;
import org.krakenapps.siem.model.ManagedLogger;
import org.krakenapps.siem.response.ResponseAction;
import org.krakenapps.siem.response.ResponseActionManager;
import org.krakenapps.siem.response.ResponseConfigOption;
import org.krakenapps.siem.response.ResponseServer;
import org.krakenapps.xmlrpc.XmlRpcFaultException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiemScript implements Script {
	private static final String JPA_FACTORY_NAME = "siem";
	private final Logger slog = LoggerFactory.getLogger(SiemScript.class.getName());
	private BundleContext bc;
	private ScriptContext context;

	public SiemScript(BundleContext bc) {
		this.bc = bc;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void load(String[] args) {
		try {
			JpaService jpa = getJpaService();
			Preferences root = getConfigRoot();

			String host = getInput("jpa.siem.host", "Database Host", "localhost");
			String databaseName = getInput("jpa.siem.db", "Database Name", "kraken");
			String username = getInput("jpa.siem.user", "Database User", "root");
			String password = getInput("jpa.siem.password", "Database Password", null, true);

			root.flush();
			root.sync();

			String connectionString = String.format("jdbc:mysql://%s/%s??useUnicode=true&amp;characterEncoding=utf8",
					host, databaseName);

			if (jpa.getEntityManagerFactory(JPA_FACTORY_NAME) == null) {
				try {
					Properties props = new Properties();
					props.put("hibernate.connection.url", connectionString);
					props.put("hibernate.connection.username", username);
					props.put("hibernate.connection.password", password);

					jpa.registerEntityManagerFactory(JPA_FACTORY_NAME, props, bc.getBundle().getBundleId());
				} catch (BundleException e) {
					context.println("cannot find model bundle");
					slog.error("kraken siem: cannot find model bundle", e);
				}
			} else {
				context.println("kraken siem database loaded");
			}

			context.println("siem loaded");
		} catch (InterruptedException e) {
			context.println("");
			context.println("interrupted");
		} catch (BackingStoreException e) {
			context.println("cannot save database configuration");
			slog.error("kraken siem: cannot save database configuration", e);
		}
	}

	public void reset(String args[]) {
		try {
			Preferences pref = getConfigRoot();

			pref.remove("jpa.siem.host");
			pref.remove("jpa.siem.db");
			pref.remove("jpa.siem.user");
			pref.remove("jpa.siem.password");

			pref.flush();
			pref.sync();

			context.println("database configuration reset success");
		} catch (BackingStoreException e) {
			context.println("cannot remove database configuration");
			slog.error("kraken siem: cannot remove database configuration", e);
		}
	}

	private String getInput(String key, String label, String defaultValue) throws InterruptedException {
		return getInput(key, label, defaultValue, false);
	}

	private String getInput(String key, String label, String defaultValue, boolean isPassword)
			throws InterruptedException {
		Preferences p = getConfigRoot();
		String oldConfig = p.get(key, null);
		if (oldConfig != null)
			return oldConfig;

		String def = ": ";
		if (defaultValue != null)
			def = " (default '" + defaultValue + "'): ";

		context.print(label + def);

		String value = null;
		if (isPassword)
			value = context.readPassword();
		else
			value = context.readLine();

		if (value.isEmpty() && defaultValue != null)
			value = defaultValue;

		p.put(key, value);

		return value;
	}

	public void unload(String[] args) {
		JpaService jpa = getJpaService();
		if (jpa.getEntityManagerFactory(JPA_FACTORY_NAME) != null) {
			jpa.unregisterEntityManagerFactory(JPA_FACTORY_NAME);
			context.println("jpa unloaded");
		}
	}

	public void loggers(String[] args) {
		try {
			LogServer logServer = getLogServer();
			context.println("Managed Loggers");
			context.println("-------------------");
			for (ManagedLogger m : logServer.getManagedLoggers()) {
				context.println(m.toString());
			}
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "", arguments = {
			@ScriptArgument(name = "offset", type = "int", description = "offset"),
			@ScriptArgument(name = "limit", type = "int", description = "limit") })
	public void events(String[] args) {
		EventServer evtServer = getEventServer();
		int offset = Integer.valueOf(args[0]);
		int limit = Integer.valueOf(args[1]);

		List<Event> events = evtServer.getEvents(offset, limit);
		for (Event event : events) {
			context.println(event.toString());
		}
	}

	public void updateHttpRules(String[] args) {
		IscHttpRuleManager manager = getIscHttpRuleManager();

		try {
			manager.update();
			context.println("update successful");
		} catch (XmlRpcFaultException e) {
			context.println("failed: " + e.getFaultString());
		} catch (Exception e) {
			context.println("failed: " + e);
			slog.error("kraken siem: cannot update rules", e);
		}
	}

	@ScriptUsage(description = "traverse logs", arguments = {
			@ScriptArgument(name = "full name", type = "string", description = "logger full name"),
			@ScriptArgument(name = "day", type = "string", description = "day, yyyyMMdd"),
			@ScriptArgument(name = "offset", type = "int", description = "offset"),
			@ScriptArgument(name = "limit", type = "int", description = "limit") })
	public void traverse(String[] args) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

			String fullName = args[0];
			Date from = dateFormat.parse(args[1]);
			int offset = Integer.valueOf(args[2]);
			int limit = Integer.valueOf(args[3]);

			Calendar cal = Calendar.getInstance();
			cal.setTime(from);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			Date to = cal.getTime();

			LogServer logServer = getLogServer();
			LogPrinter printer = new LogPrinter();
			logServer.search(fullName, from, to, offset, limit, null, printer);

		} catch (Exception e) {
			context.println("cannot parse date");
			slog.error("kraken siem: cannot parse date", e);
		}
	}

	@ScriptUsage(description = "insert dummy log", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "logger full name"),
			@ScriptArgument(name = "word", type = "string", description = "any kinds of text") })
	public void insertDummyLog(String[] args) {
		String tableName = args[0];
		Date date = new Date();
		String line = args[1];

		LogServer logServer = getLogServer();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("line", line);

		logServer.write(new Log(tableName, date, data));
		context.println("wrote one log");
	}

	@ScriptUsage(description = "insert dummy event", arguments = { @ScriptArgument(type = "int", name = "org", description = "organization id") })
	public void insertDummyEvent(String[] args) throws UnknownHostException {
		Event e = new Event();
		e.setOrganizationId(Integer.valueOf(args[0]));
		e.setCategory("Attack");
		e.setSourceIp(InetAddress.getByName("202.181.239.52"));
		e.setDestinationIp(InetAddress.getByName("110.45.142.130"));
		e.setDestinationPort(80);
		e.setFirstSeen(new Date());
		e.setLastSeen(new Date());
		e.setSeverity(EventSeverity.Critical);
		e.setMessageKey("http-request-attack");
		e.setCount(1);

		EventDispatcher d = getEventDispatcher();
		d.dispatch(e);
	}

	private class LogPrinter implements LogSearchCallback {
		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		@Override
		public void interrupt() {
		}

		@Override
		public boolean isInterrupted() {
			return false;
		}

		@Override
		public void onLog(Log log) {
			String date = dateFormat.format(log.getDate());
			String line = String.format("day=%s, id=%d", date, log.getId());
			StringBuilder sb = new StringBuilder(line);

			for (String key : log.getData().keySet()) {
				sb.append(", ");
				sb.append(key);
				sb.append("=");
				sb.append(log.getData().get(key));
			}

			context.println(sb.toString());
		}
	}

	@ScriptUsage(description = "create managed logger", arguments = {
			@ScriptArgument(name = "org id", type = "int", description = "organization id"),
			@ScriptArgument(name = "logger fullname", type = "string", description = "logger fullname"),
			@ScriptArgument(name = "parser name", type = "string", description = "log parser name") })
	public void createLogger(String[] args) {
		try {
			LogServer logServer = getLogServer();
			logServer.createManagedLogger(Integer.valueOf(args[0]), args[1], args[2], new Properties());
			context.println("created");
		} catch (Exception e) {
			context.println(e.getMessage());
			slog.error("kraken siem: cannot create logger", e);
		}
	}

	@ScriptUsage(description = "remove managed logger", arguments = { @ScriptArgument(name = "logger fullname", type = "string", description = "logger fullname") })
	public void removeLogger(String[] args) {
		try {
			LogServer logServer = getLogServer();
			ManagedLogger m = logServer.getManagedLogger(args[0]);
			if (m == null) {
				context.println("logger not found");
				return;
			}

			logServer.removeManagedLogger(m.getId());
			context.println("removed");
		} catch (Exception e) {
			context.println(e.getMessage());
			slog.error("kraken siem: cannot remove logger", e);
		}
	}

	@ScriptUsage(description = "print all response action managers")
	public void responseManagers(String[] args) {
		ResponseServer responseServer = getResponseServer();
		if (responseServer == null) {
			context.println("response server not ready");
			return;
		}

		context.println("Response Action Managers");
		context.println("-------------------------");
		for (ResponseActionManager manager : responseServer.getResponseActionManagers()) {
			context.println(manager.toString());
		}
	}

	@ScriptUsage(description = "create new response action", arguments = {
			@ScriptArgument(name = "manager name", type = "string", description = "the name of response action manager"),
			@ScriptArgument(name = "namespace", type = "string", description = "namespace"),
			@ScriptArgument(name = "name", type = "string", description = "name") })
	public void createResponseAction(String[] args) {
		ResponseServer responseServer = getResponseServer();
		if (responseServer == null) {
			context.println("response server not ready");
			return;
		}

		String managerName = args[0];
		String namespace = args[1];
		String name = args[2];

		ResponseActionManager manager = responseServer.getResponseActionManager(managerName);
		if (manager == null) {
			context.println("manager not found");
			return;
		}

		try {
			Properties config = new Properties();
			for (ResponseConfigOption option : manager.getConfigOptions()) {
				String label = option.getDisplayName(Locale.ENGLISH);
				if (!option.isRequired())
					label += " (optional)";

				context.print(label + ": ");
				String value = context.readLine();

				if (!value.isEmpty())
					config.put(option.getName(), value);
				else if (option.isRequired()) {
					context.println("this is required option");
					return;
				}
			}

			manager.newAction(namespace, name, null, config);
			context.println("created");
		} catch (InterruptedException e) {
			context.println("");
			context.println("interrupted");
		} catch (Exception e) {
			context.println(e.getMessage());
			slog.error("kraken siem: cannot create new action", e);
		}
	}

	@ScriptUsage(description = "remove response action", arguments = {
			@ScriptArgument(name = "manager", type = "string", description = "manager name"),
			@ScriptArgument(name = "namespace", type = "string", description = "action namespace"),
			@ScriptArgument(name = "name", type = "string", description = "action name") })
	public void removeResponseAction(String[] args) {
		ResponseServer responseServer = getResponseServer();
		if (responseServer == null) {
			context.println("response server not ready");
			return;
		}

		String managerName = args[0];
		String namespace = args[1];
		String name = args[2];

		ResponseActionManager manager = responseServer.getResponseActionManager(managerName);
		if (manager == null) {
			context.println("manager not found");
			return;
		}

		manager.deleteAction(namespace, name);
		context.println("deleted");
	}

	@ScriptUsage(description = "print response actions", arguments = {
			@ScriptArgument(name = "manager name", type = "string", description = "name of the response manager"),
			@ScriptArgument(name = "namespace", type = "string", description = "action namespace", optional = true) })
	public void responseActions(String[] args) {
		ResponseServer responseServer = getResponseServer();
		if (responseServer == null) {
			context.println("response server not ready");
			return;
		}

		ResponseActionManager manager = responseServer.getResponseActionManager(args[0]);
		if (manager == null) {
			context.println("manager not found");
			return;
		}

		Collection<ResponseAction> actions = null;
		if (args.length > 1)
			actions = manager.getActions(args[1]);
		else
			actions = manager.getActions();

		context.println("Response Actions");
		context.println("------------------");
		for (ResponseAction action : actions) {
			context.println("manager=" + action.getManager() + ", namespace=" + action.getNamespace() + ", name="
					+ action.getName() + " => " + action.toString());
		}
	}

	@ScriptUsage(description = "print all response mappings")
	public void responseMappings(String[] args) {
		EventResponseMapper mapper = getEventResponseMapper();
		if (mapper == null) {
			context.println("mapper not ready");
			return;
		}

		context.println("Event-Response Mappings");
		context.println("-------------------------");
		for (ResponseKey key : mapper.getKeys()) {
			context.println(key.toString());
			Collection<ResponseAction> actions = mapper.getActions(key);
			for (ResponseAction action : actions) {
				context.println(" " + action.toString());
			}

			context.println("");
		}
	}

	@ScriptUsage(description = "create event-response mapping", arguments = {
			@ScriptArgument(name = "category", type = "string", description = "event category name"),
			@ScriptArgument(name = "manager", type = "string", description = "manager name"),
			@ScriptArgument(name = "namespace", type = "string", description = "namespace"),
			@ScriptArgument(name = "name", type = "string", description = "name") })
	public void createResponseMapping(String[] args) {
		ResponseServer responseServer = getResponseServer();
		if (responseServer == null) {
			context.println("response server not ready");
			return;
		}

		EventResponseMapper mapper = getEventResponseMapper();
		if (mapper == null) {
			context.println("mapper not ready");
			return;
		}

		String category = args[0];
		String managerName = args[1];
		String namespace = args[2];
		String name = args[3];

		ResponseActionManager manager = responseServer.getResponseActionManager(managerName);
		ResponseAction action = manager.getAction(namespace, name);
		if (action == null) {
			context.println("action not found");
			return;
		}

		mapper.addResponse(new ResponseKey(category), action);
		context.println("created");
	}

	@ScriptUsage(description = "remove event-response mapping", arguments = {
			@ScriptArgument(name = "category", type = "string", description = "event category name"),
			@ScriptArgument(name = "manager", type = "string", description = "manager name"),
			@ScriptArgument(name = "namespace", type = "string", description = "namespace"),
			@ScriptArgument(name = "name", type = "string", description = "name") })
	public void removeResponseMapping(String[] args) {
		ResponseServer responseServer = getResponseServer();
		if (responseServer == null) {
			context.println("response server not ready");
			return;
		}

		EventResponseMapper mapper = getEventResponseMapper();
		if (mapper == null) {
			context.println("mapper not ready");
			return;
		}

		String category = args[0];
		String managerName = args[1];
		String namespace = args[2];
		String name = args[3];

		ResponseActionManager manager = responseServer.getResponseActionManager(managerName);
		ResponseAction action = manager.getAction(namespace, name);

		mapper.removeResponse(new ResponseKey(category), action);
		context.println("removed");
	}

	public void configure(String[] args) {
		LogFileScannerRegistry registry = getLogFileScannerRegistry();
		if (registry == null) {
			context.println("log file scanner registry not ready");
			return;
		}

		LoggerFactoryRegistry loggerFactoryRegistry = getLoggerFactoryRegistry();
		if (loggerFactoryRegistry == null) {
			context.println("logger factory registry not ready");
			return;
		}

		org.krakenapps.log.api.LoggerRegistry loggerRegistry = getLoggerRegistry();
		if (loggerRegistry == null) {
			context.println("logger registry not ready");
			return;
		}

		org.krakenapps.log.api.LoggerFactory factory = loggerFactoryRegistry.getLoggerFactory("textfile");
		if (factory == null) {
			context.println("text logger factory not found");
			return;
		}

		LogServer logServer = getLogServer();
		if (logServer == null) {
			context.println("log server not ready");
			return;
		}

		ResponseServer responseServer = getResponseServer();
		if (responseServer == null) {
			context.println("response server not ready");
			return;
		}

		EventResponseMapper eventResponseMapper = getEventResponseMapper();
		if (eventResponseMapper == null) {
			context.println("event response mapper not ready");
			return;
		}

		// add default event-response
		ResponseActionManager manager = responseServer.getResponseActionManager("firewall");
		if (manager.getAction("local", "all") == null) {
			Properties config = new Properties();
			config.put("group_name", "all");
			config.put("minutes", "60");
			manager.newAction("local", "all", "", config);
			context.println("created default firewall response action");
		}

		ResponseKey responseKey = new ResponseKey("Attack");
		if (eventResponseMapper.getActions(responseKey) == null) {
			ResponseAction action = manager.getAction("local", "all");
			eventResponseMapper.addResponse(responseKey, action);
			context.println("firewall will block source ip which are detected as attack event");
		}

		ServiceReference ref = bc.getServiceReference(OrganizationApi.class.getName());
		OrganizationApi orgApi = (OrganizationApi) bc.getService(ref);

		// find and add loggers
		for (LogFileScanner scanner : registry.getScanners()) {
			context.println("running " + scanner.getName() + " scanner");
			for (CandidateTextFileLogger candidate : scanner.scan()) {
				try {
					context.println(" " + candidate.toString());

					org.krakenapps.log.api.Logger logger = loggerRegistry.getLogger("local", candidate.getName());
					if (logger != null) {
						context.println("  " + candidate.getName() + " logger already exists");

					} else {
						Properties config = new Properties();
						config.put("file.path", candidate.getFile().getAbsolutePath());
						config.put("date.pattern", candidate.getDatePattern());
						config.put("date.locale", candidate.getDateLocale());

						logger = factory.newLogger(candidate.getName(), "", config);
						context.println("  logger [" + logger.getFullName() + "] created");
					}

					if (logServer.getManagedLogger(logger.getFullName()) == null) {
						for (Organization org : orgApi.getOrganizations()) {
							logServer.createManagedLogger(org.getId(), logger.getFullName(),
									candidate.getParserFactoryName(), candidate.getParserOptions());
							context.println("  managed logger [org=" + org.getId() + ", " + logger.getFullName()
									+ "] created, parser [" + candidate.getParserFactoryName() + "] mapped");
						}
					}

					if (!logger.isRunning()) {
						logger.start(1000); // check every 1sec
						context.println("  logger [" + logger.getFullName() + "] started");
					}
				} catch (Exception e) {
					context.println(e.getMessage());
				}
			}
		}
	}

	public void logFileScanners(String[] args) {
		LogFileScannerRegistry registry = getLogFileScannerRegistry();
		if (registry == null) {
			context.println("log file scanner registry not ready");
			return;
		}

		context.println("Log File Scanners");
		context.println("-------------------------");
		for (LogFileScanner scanner : registry.getScanners()) {
			context.println(scanner.getName() + ": " + scanner.toString());
		}
	}

	private LoggerFactoryRegistry getLoggerFactoryRegistry() {
		return getService(LoggerFactoryRegistry.class, "logger factory registry");
	}

	private LoggerRegistry getLoggerRegistry() {
		return getService(LoggerRegistry.class, "logger registry");
	}

	private LogFileScannerRegistry getLogFileScannerRegistry() {
		return getService(LogFileScannerRegistry.class, "log file scanner registry");
	}

	private EventResponseMapper getEventResponseMapper() {
		return getService(EventResponseMapper.class, "event-response mapper");
	}

	private JpaService getJpaService() {
		return getService(JpaService.class, "jpa service");
	}

	private LogServer getLogServer() {
		return getService(LogServer.class, "log server");
	}

	private ResponseServer getResponseServer() {
		return getService(ResponseServer.class, "response server");
	}

	private EventDispatcher getEventDispatcher() {
		return getService(EventDispatcher.class, "event dispatcher");
	}

	private EventServer getEventServer() {
		return getService(EventServer.class, "event server");
	}

	private IscHttpRuleManager getIscHttpRuleManager() {
		return getService(IscHttpRuleManager.class, "isc http-rule manager");
	}

	private Preferences getConfigRoot() {
		PreferencesService prefsvc = getService(PreferencesService.class, "preferences service");
		return prefsvc.getSystemPreferences();
	}

	@SuppressWarnings("unchecked")
	private <T> T getService(Class<T> cls, String name) {
		ServiceReference ref = bc.getServiceReference(cls.getName());
		if (ref == null)
			throw new IllegalStateException(name + " not ready");

		return (T) bc.getService(ref);
	}
}
