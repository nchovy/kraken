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
import java.util.ArrayList;
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
import org.krakenapps.dom.api.ProgramApi;
import org.krakenapps.dom.model.Program;
import org.krakenapps.dom.model.ProgramPack;
import org.krakenapps.dom.model.ProgramProfile;
import org.krakenapps.event.api.Event;
import org.krakenapps.event.api.EventDispatcher;
import org.krakenapps.event.api.EventSeverity;
import org.krakenapps.log.api.LoggerFactoryRegistry;
import org.krakenapps.log.api.LoggerRegistry;
import org.krakenapps.logstorage.Log;
import org.krakenapps.siem.CandidateTextFileLogger;
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
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiemScript implements Script {
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

	public void install(String[] args) {
		String domain = "localhost";
		if (args.length > 0)
			domain = args[0];

		ServiceReference ref = bc.getServiceReference(ProgramApi.class.getName());
		ProgramApi programApi = (ProgramApi) bc.getService(ref);

		if (programApi.findProgramPack(domain, "radar") == null) {
			ProgramPack pack = new ProgramPack();
			pack.setName("Radar");
			pack.setDll("radar");
			pack.setStarter("starter");
			pack.setSeq(1);
			programApi.createProgramPack(domain, pack);
		}

		List<Program> programs = new ArrayList<Program>();
		programs.add(createProgram(programApi, domain, "Radar", "Log Query", "logquery", 1));
		programs.add(createProgram(programApi, domain, "Radar", "Log Source", "logsource", 2));

		ProgramProfile pp = programApi.findProgramProfile(domain, "all");
		pp.getPrograms().addAll(programs);
		programApi.updateProgramProfile(domain, pp);
		context.println("installed");
	}

	private Program createProgram(ProgramApi programApi, String domain, String pack, String name, String path, int seq) {
		Program program = new Program();
		program.setPack(pack);
		program.setName(name);
		program.setPath(path);
		program.setSeq(seq);
		program.setVisible(true);

		programApi.createProgram(domain, program);
		return program;
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

	@ScriptUsage(description = "insert dummy event", arguments = {
			@ScriptArgument(type = "string", name = "org", description = "organization domain"),
			@ScriptArgument(type = "string", name = "severity", description = "severity"),
			@ScriptArgument(type = "string", name = "src ip", description = "src ip"),
			@ScriptArgument(type = "string", name = "dst ip", description = "dst ip"),
			@ScriptArgument(type = "string", name = "cve", description = "cve") })
	public void insertDummyEvent(String[] args) throws UnknownHostException {
		String domain = args[0];
		EventSeverity severity = EventSeverity.valueOf(args[1]);
		InetAddress src = InetAddress.getByName(args[2]);
		InetAddress dst = InetAddress.getByName(args[3]);

		Event e = new Event();
		e.setOrgDomain(domain);
		e.setCategory("Attack");
		e.setSourceIp(src);
		e.setDestinationIp(dst);
		e.setDestinationPort(80);
		e.setFirstSeen(new Date());
		e.setLastSeen(new Date());
		e.setSeverity(severity);
		e.setMessageKey("http-request-attack");
		e.setCount(1);

		EventDispatcher d = getEventDispatcher();
		d.dispatch(e);
	}

	@ScriptUsage(description = "create managed logger", arguments = {
			@ScriptArgument(name = "org domain", type = "string", description = "org domain"),
			@ScriptArgument(name = "logger fullname", type = "string", description = "logger fullname") })
	public void createLogger(String[] args) {
		try {
			LogServer logServer = getLogServer();
			ManagedLogger ml = new ManagedLogger();
			ml.setOrgDomain(args[0]);
			ml.setFullName(args[1]);

			logServer.createManagedLogger(ml);
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

			logServer.removeManagedLogger(m);
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
		final String orgDomain = "localhost";

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
						Properties config = new Properties(candidate.getMetadata());
						config.put("file.path", candidate.getFile().getAbsolutePath());

						logger = factory.newLogger(candidate.getName(), "", config);
						context.println("  logger [" + logger.getFullName() + "] created");
					}

					if (logServer.getManagedLogger(logger.getFullName()) == null) {
						ManagedLogger ml = new ManagedLogger();
						ml.setOrgDomain(orgDomain);
						ml.setFullName(logger.getFullName());
						ml.setMetadata(toMap(candidate.getMetadata()));

						context.println("  managed logger [org=" + orgDomain + ", " + logger.getFullName() + "] created");
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

	private Map<String, String> toMap(Properties p) {
		Map<String, String> m = new HashMap<String, String>();
		for (Object key : p.keySet())
			m.put(key.toString(), p.getProperty(key.toString()));
		return m;
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

	private ProgramApi getProgramApi() {
		return getService(ProgramApi.class, "program api");
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

	private LogServer getLogServer() {
		return getService(LogServer.class, "log server");
	}

	private ResponseServer getResponseServer() {
		return getService(ResponseServer.class, "response server");
	}

	private EventDispatcher getEventDispatcher() {
		return getService(EventDispatcher.class, "event dispatcher");
	}

	private IscHttpRuleManager getIscHttpRuleManager() {
		return getService(IscHttpRuleManager.class, "isc http-rule manager");
	}

	@SuppressWarnings("unchecked")
	private <T> T getService(Class<T> cls, String name) {
		ServiceReference ref = bc.getServiceReference(cls.getName());
		if (ref == null)
			throw new IllegalStateException(name + " not ready");

		return (T) bc.getService(ref);
	}
}
