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
package org.krakenapps.sentry.impl;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.log.api.Log;
import org.krakenapps.log.api.LogPipe;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerEventListener;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerFactoryRegistry;
import org.krakenapps.log.api.LoggerRegistry;
import org.krakenapps.log.api.LoggerRegistryEventListener;
import org.krakenapps.rpc.RpcContext;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcSession;
import org.krakenapps.sentry.Base;
import org.krakenapps.sentry.Sentry;
import org.krakenapps.sentry.SentryCommandHandler;
import org.krakenapps.sentry.SentryMethod;

@Component(name = "sentry-basic-command-handler")
@Provides(specifications = { SentryCommandHandler.class })
public class BasicCommandHandler implements SentryCommandHandler, LoggerRegistryEventListener, LogPipe,
		LoggerEventListener {
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BasicCommandHandler.class.getName());

	@Requires
	private Sentry sentry;

	@Requires
	private LoggerRegistry loggerRegistry;

	@Requires
	private LoggerFactoryRegistry loggerFactoryRegistry;

	// one connected logger to multiple bases
	private ConcurrentMap<String, Set<String>> connectedLoggers;

	public BasicCommandHandler() {
		connectedLoggers = new ConcurrentHashMap<String, Set<String>>();
	}

	@Validate
	public void start() {
		connectedLoggers.clear();
		loggerRegistry.addListener(this);
	}

	@Invalidate
	public void stop() {
		if (loggerRegistry != null) {
			loggerRegistry.removeListener(this);

			for (String loggerName : connectedLoggers.keySet()) {
				Logger logger = loggerRegistry.getLogger("local", loggerName);
				if (logger != null) {
					logger.removeLogPipe(this);
					logger.removeEventListener(this);
				}
			}
		}

		connectedLoggers.clear();
	}

	@Override
	public Collection<String> getFeatures() {
		return Arrays.asList("logger-control", "disk-info", "nic-info");
	}

	@SentryMethod
	public List<Object> getLoggerFactories() {
		List<Object> l = new ArrayList<Object>();

		// send only local factories
		for (LoggerFactory factory : loggerFactoryRegistry.getLoggerFactories())
			if (factory.getNamespace().equals("local"))
				l.add(LoggerFactorySerializer.toMap(factory));

		return l;
	}

	@SentryMethod
	public Object getLoggerFactory(String name) {
		log.trace("kraken sentry: querying get logger factory [{}]", name);
		return LoggerFactorySerializer.toMap(loggerFactoryRegistry.getLoggerFactory(name));
	}

	@SentryMethod
	public List<Object> getLoggers() {
		List<Object> l = new ArrayList<Object>();

		// send only loggers in local namespace
		for (Logger logger : loggerRegistry.getLoggers())
			if (logger.getNamespace().equals("local"))
				l.add(LoggerFactorySerializer.toMap(logger));

		return l;
	}

	@SentryMethod
	public void createLogger(String factoryName, String name, String description, Map<String, Object> props) {
		LoggerFactory factory = loggerFactoryRegistry.getLoggerFactory(factoryName);
		if (factory == null)
			throw new RpcException("logger factory not found: " + factoryName);

		Properties config = convert(props);

		log.trace("kraken sentry: create logger [factory={}, name={}, desc={}] with config [{}]", new Object[] {
				factory, name, description, buildConfigLog(config) });

		factory.newLogger(name, description, config);
	}

	private String buildConfigLog(Properties config) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (Object key : config.keySet()) {
			if (i != 0)
				sb.append(", ");
			sb.append(key.toString());
			sb.append("=");
			sb.append(config.get(key));
			i++;
		}

		return sb.toString();
	}

	private Properties convert(Map<String, Object> props) {
		Properties p = new Properties();
		for (String key : props.keySet()) {
			p.put(key, props.get(key));
		}
		return p;
	}

	/**
	 * 
	 */
	@SentryMethod
	public void removeLogger(String name) {
		Logger logger = loggerRegistry.getLogger("local", name);
		if (logger == null)
			throw new RpcException("logger not found: " + name);

		if (logger.isRunning())
			throw new RpcException("logger is running: " + name);

		LoggerFactory factory = loggerFactoryRegistry.getLoggerFactory(logger.getFactoryNamespace(),
				logger.getFactoryName());

		factory.deleteLogger(name);
	}

	@SentryMethod
	public void startLogger(String name, int interval) {
		Logger logger = loggerRegistry.getLogger("local", name);
		if (logger == null)
			throw new RpcException("logger not found: " + name);

		try {
			logger.start(interval);
		} catch (Exception e) {
			throw new RpcException(e.getMessage());
		}
	}

	@SentryMethod
	public void stopLogger(String name, int timeout) {
		Logger logger = loggerRegistry.getLogger("local", name);
		if (logger == null)
			throw new RpcException("logger not found: " + name);

		try {
			logger.stop(timeout);
		} catch (Exception e) {
			throw new RpcException(e.getMessage());
		}
	}

	@SentryMethod
	public Map<String, Object> connectLogger(String name) {
		Logger logger = loggerRegistry.getLogger("local", name);
		if (logger == null)
			throw new RpcException("logger not found: " + name);

		String baseName = sentry.getBaseName(RpcContext.getConnection());
		if (baseName == null)
			throw new RpcException("base not found: " + baseName);

		try {
			// add log pipe
			logger.addLogPipe(this);

			// add to connected logger list
			Set<String> baseNames = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
			Set<String> oldBaseNames = connectedLoggers.putIfAbsent(name, baseNames);
			if (oldBaseNames != null)
				baseNames = oldBaseNames;

			baseNames.add(baseName);

			log.info("kraken sentry: logger [{}] connected", name);

			return LoggerFactorySerializer.toMap(logger);
		} catch (Exception e) {
			log.error("kraken sentry: connect logger failed", e);
			throw new RpcException("connect logger failed", e);
		}
	}

	@SentryMethod
	public void disconnectLogger(String name) {
		Logger logger = loggerRegistry.getLogger("local", name);
		if (logger == null)
			throw new RpcException("logger not found: " + name);

		String baseName = sentry.getBaseName(RpcContext.getConnection());
		if (baseName == null)
			throw new RpcException("base not found: " + name);

		try {
			Set<String> baseNames = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
			Set<String> oldBaseNames = connectedLoggers.putIfAbsent(name, baseNames);
			if (oldBaseNames != null)
				baseNames = oldBaseNames;

			baseNames.remove(baseName);

			// remove pipe only if reference count reaches 0
			if (baseNames.size() == 0) {
				logger.removeLogPipe(this);
				connectedLoggers.remove(name);
			}

		} catch (Exception e) {
			throw new RpcException("disconnect logger failed", e);
		}
	}

	@SentryMethod
	public void connectLogChannel(String nonce) {
		try {
			String baseName = sentry.getBaseName(RpcContext.getConnection());
			if (baseName == null)
				throw new RpcException("base name not found");

			sentry.connectLogChannel(baseName, nonce);
		} catch (Exception e) {
			throw new RpcException(e.getMessage(), e);
		}
	}

	@SentryMethod
	public void disconnectLogChannel() {
		String baseName = sentry.getBaseName(RpcContext.getConnection());
		sentry.disconnectLogChannel(baseName);
	}

	@SentryMethod
	public Date getDate() {
		return new Date();
	}

	@SentryMethod
	public List<Object> listFiles() {
		return listFiles(null);
	}

	@SentryMethod
	public List<Object> listFiles(String path) {
		if (path == null)
			return toList(File.listRoots());

		return toList(new File(path).listFiles());
	}

	private List<Object> toList(File[] files) {
		List<Object> l = new ArrayList<Object>(files.length);
		for (File file : files)
			l.add(toMap(file));
		return l;
	}

	private Map<String, Object> toMap(File file) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", file.getName());
		m.put("size", file.length());
		m.put("is_dir", file.isDirectory());
		m.put("modified_at", new Date(file.lastModified()));
		return m;
	}

	@SentryMethod
	public String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new RpcException("hostname not found");
		}
	}

	@SentryMethod
	public Map<String, String> getSystemInfo() {
		String[] keys = new String[] { "java.vendor", "java.version", "java.vm.name", "java.vm.vendor",
				"java.vm.version", "os.name", "os.arch", "os.version", "user.dir", "user.home" };

		Map<String, String> m = new HashMap<String, String>();
		for (String key : keys) {
			m.put(key, System.getProperty(key));
		}

		m.put("host.name", getHostName());
		return m;
	}

	@SentryMethod
	public Map<String, Object> getDiskPartitions() {
		Map<String, Object> partitions = new HashMap<String, Object>();
		for (File root : File.listRoots()) {
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("total", root.getTotalSpace());
			info.put("free", root.getFreeSpace());

			try {
				partitions.put(root.getCanonicalPath(), info);
			} catch (IOException e) {
			}
		}

		return partitions;
	}

	@SentryMethod
	public List<Object> getNetworkInterfaces() {
		List<Object> infs = new ArrayList<Object>();
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				NetworkInterface ni = en.nextElement();
				if (ni.getHardwareAddress() == null || ni.getHardwareAddress().length != 6)
					continue;

				infs.add(toMap(ni));
			}
			return infs;
		} catch (SocketException e) {
			throw new RpcException(e.getMessage());
		}
	}

	private Map<String, Object> toMap(NetworkInterface ni) throws SocketException {
		Map<String, Object> m = new HashMap<String, Object>();
		InetAddress ip4addr = getIpAddress(ni, false);
		InetAddress ip6addr = getIpAddress(ni, true);

		m.put("display_name", ni.getDisplayName());
		m.put("name", ni.getName());
		m.put("mac", toMacAddress(ni.getHardwareAddress()));
		m.put("ip", ip4addr == null ? null : ip4addr.getHostAddress());
		m.put("ip6", ip6addr == null ? null : ip6addr.getHostAddress());
		m.put("mtu", ni.getMTU());
		return m;
	}

	private InetAddress getIpAddress(NetworkInterface ni, boolean findIp6) {
		Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
		while (inetAddresses.hasMoreElements()) {
			InetAddress addr = inetAddresses.nextElement();
			if (findIp6 && addr instanceof Inet6Address)
				return addr;
			else if (addr instanceof Inet4Address)
				return addr;
		}

		return null;
	}

	private String toMacAddress(byte[] b) {
		if (b == null || b.length != 6)
			return null;

		return String.format("%02x:%02x:%02x:%02x:%02x:%02x", b[0], b[1], b[2], b[3], b[4], b[5]);
	}

	@Override
	public void onLog(Logger logger, Log log) {
		String loggerName = logger.getName();
		Set<String> baseNames = connectedLoggers.get(loggerName);
		if (baseNames == null) {
			this.log.warn("kraken-sentry: no base connection with logger {}", loggerName);
			return;
		}

		Map<String, Object> logData = convert(log);

		for (String baseName : baseNames) {
			RpcSession logSession = sentry.getLogSession(baseName);
			if (logSession == null) {
				this.log.trace("kraken-sentry: log session not found for base [{}]", baseName);
				continue;
			}

			try {
				logSession.post("onLog", new Object[] { sentry.getGuid(), loggerName, logData });
			} catch (Exception e) {
				this.log.error("kraken-sentry: log callback failed, base=" + baseName, e);
			}
		}
	}

	private Map<String, Object> convert(Log log) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("date", log.getDate());
		m.put("msg", log.getMessage());
		m.put("params", log.getParams());
		return m;
	}

	@Override
	public void loggerAdded(Logger logger) {
		if (!logger.getNamespace().equals("local"))
			return;

		logger.addEventListener(this);
		// NOTE: connect log pipe when logger is connected
	}

	@Override
	public void loggerRemoved(Logger logger) {
		if (!logger.getNamespace().equals("local"))
			return;

		connectedLoggers.remove(logger.getName());

		logger.removeLogPipe(this);
		logger.removeEventListener(this);
	}

	public void sessionClosed(RpcSession session) {
		String baseName = sentry.getBaseName(session.getConnection());

		log.info("kraken sentry: base [{}] disconnected, clean up states", baseName);
		for (String loggerName : new ArrayList<String>(connectedLoggers.keySet())) {
			Set<String> baseNames = connectedLoggers.get(loggerName);
			baseNames.remove(baseName);
		}
	}

	//
	// LoggerEventListener callback
	//

	@Override
	public void onStart(Logger logger) {
		if (!logger.getNamespace().equals("local"))
			return;

		log.trace("kraken sentry: starting logger {}", logger.getFullName());

		// notify logger started event
		for (Base base : sentry.getBases()) {
			RpcSession commandSession = sentry.getCommandSession(base.getName());
			if (commandSession == null) {
				log.error("kraken sentry: logger started notification failed, session not found for {}", base.getName());
				continue;
			}

			try {
				commandSession.post("loggerStarted", new Object[] { sentry.getGuid(), logger.getName(),
						logger.getInterval() });
			} catch (Exception e) {
				log.error("kraken sentry: cannot post logger started event", e);
			}
		}
	}

	@Override
	public void onStop(Logger logger) {
		if (!logger.getNamespace().equals("local"))
			return;

		log.trace("kraken sentry: stopping logger {}", logger.getFullName());
		if (sentry == null)
			return;

		// notify logger stopped event
		for (Base base : sentry.getBases()) {
			RpcSession commandSession = sentry.getCommandSession(base.getName());
			if (commandSession == null) {
				log.error("kraken sentry: logger stopped notification failed, session not found for {}", base.getName());
				continue;
			}

			try {
				commandSession.post("loggerStopped", new Object[] { sentry.getGuid(), logger.getName() });
			} catch (Exception e) {
				log.error("kraken sentry: cannot post logger stopped event", e);
			}
		}
	}

	@Override
	public void onUpdated(Logger logger, Properties config) {
		// ignored. remote propagation not needed
	}
}
