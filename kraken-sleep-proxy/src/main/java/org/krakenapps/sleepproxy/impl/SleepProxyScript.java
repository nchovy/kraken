package org.krakenapps.sleepproxy.impl;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.filter.FilterManager;
import org.krakenapps.jpa.JpaService;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.sleepproxy.ConfigKey;
import org.krakenapps.sleepproxy.ConfigStore;
import org.krakenapps.sleepproxy.PowerStat;
import org.krakenapps.sleepproxy.SleepProxyApi;
import org.krakenapps.sleepproxy.TrafficMonitor;
import org.krakenapps.sleepproxy.model.SleepLog;
import org.krakenapps.syslog.SyslogServerRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

public class SleepProxyScript implements Script {
	private BundleContext bc;
	private ScriptContext context;

	public SleepProxyScript(BundleContext bc) {
		this.bc = bc;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}
	
	public void arpCache(String[] args) {
		TrafficMonitor tm = getTrafficMonitor();
		if (tm == null) {
			context.println("traffic monitor service not ready");
			return;
		}
		
		Map<InetAddress, MacAddress> arpCache = tm.getArpCache();
		for (InetAddress ip : arpCache.keySet()) {
			context.println(ip + ": " + arpCache.get(ip));
		}
	}

	@ScriptUsage(description = "force wake", arguments = { @ScriptArgument(name = "agent id", type = "int", description = "agent id") })
	public void wake(String[] args) {
		try {
			SleepProxyApi api = getSleepProxyApi();
			if (api == null) {
				context.println("sleep proxy service not ready");
				return;
			}
			api.wakeAgent(Integer.valueOf(args[0]));
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "lock ip", arguments = { @ScriptArgument(name = "ip", type = "string", description = "ip address") })
	public void lock(String[] args) {
		try {
			InetAddress ip = InetAddress.getByName(args[0]);
			TrafficMonitor tm = getTrafficMonitor();
			tm.register(ip);
			context.println("locked");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "unlock ip", arguments = { @ScriptArgument(name = "ip", type = "string", description = "ip address") })
	public void unlock(String[] args) {
		try {
			InetAddress ip = InetAddress.getByName(args[0]);
			TrafficMonitor tm = getTrafficMonitor();
			tm.unregister(ip);
			context.println("unlocked");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "get logs", arguments = {
			@ScriptArgument(name = "from", type = "string", description = "from date (yyyy-MM-dd)"),
			@ScriptArgument(name = "to", type = "string", description = "to date (yyyy-MM-dd)"),
			@ScriptArgument(name = "group_id", type = "int", description = "group id") })
	public void logs(String[] args) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date from = dateFormat.parse(args[0]);
			Date to = dateFormat.parse(args[1]);
			int groupId = Integer.parseInt(args[2]);

			SleepProxyApi sleepProxyApi = getSleepProxyApi();

			Collection<SleepLog> logs = sleepProxyApi.getLogs(groupId, from, to);

			for (SleepLog log : logs) {
				context.println(log.toString());
			}
		} catch (ParseException e) {
			context.println("cannot parse date format");
		}
	}

	public void params(String[] args) {
		context.println("Parameters");
		context.println("---------------------");
		printParam(ConfigKey.PolicyUrl);
		printParam(ConfigKey.PolicyInterval);
		printParam(ConfigKey.SyslogIP);
		printParam(ConfigKey.SyslogPort);
		printParam(ConfigKey.HeartbeatInterval);
	}

	@ScriptUsage(description = "set parameter", arguments = {
			@ScriptArgument(name = "key", type = "string", description = "key name"),
			@ScriptArgument(name = "value", type = "string", description = "value") })
	public void set(String[] args) {
		ConfigStore cs = getConfigStore();
		if (cs == null) {
			context.println("config store not ready");
			return;
		}

		cs.set(args[0], args[1]);
	}

	@ScriptUsage(description = "delete parameter", arguments = { @ScriptArgument(name = "key", type = "string", description = "key name") })
	public void delete(String[] args) {
		ConfigStore cs = getConfigStore();
		if (cs == null) {
			context.println("config store not ready");
			return;
		}

		cs.delete(args[0]);
		context.println("deleted");
	}

	public void reset(String[] args) {
		ConfigStore cs = getConfigStore();
		if (cs == null) {
			context.println("config store not ready");
			return;
		}

		cs.delete(ConfigKey.PolicyUrl);
		cs.delete(ConfigKey.PolicyInterval);
		cs.delete(ConfigKey.SyslogIP);
		cs.delete(ConfigKey.SyslogPort);
		cs.delete(ConfigKey.HeartbeatInterval);
		context.println("completed");
	}

	private void printParam(String key) {
		ConfigStore cs = getConfigStore();
		if (cs == null) {
			context.println("config store not ready");
			return;
		}

		context.println(key + ": " + cs.get(key));
	}

	@ScriptUsage(description = "print group power graph", arguments = {
			@ScriptArgument(name = "group id", type = "int", description = "group id"),
			@ScriptArgument(name = "from", type = "string", description = "from (yyyyMMddHHmm)"),
			@ScriptArgument(name = "to", type = "string", description = "to (yyyyMMddHHmm)") })
	public void graph(String[] args) {
		SleepProxyApi api = getSleepProxyApi();
		if (api == null) {
			context.println("sleep proxy service not ready");
			return;
		}

		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");

			int groupId = Integer.valueOf(args[0]);
			Date from = dateFormat.parse(args[1]);
			Date to = dateFormat.parse(args[2]);

			Collection<PowerStat> stats = api.getGroupPowerGraph(groupId, from, to, true);
			for (PowerStat stat : stats)
				context.println(stat.toString());

		} catch (ParseException e) {
			context.println("date format should be yyyyMMddHHmm");
		}
	}

	public void load(String[] args) {
		// check OSGi services
		JpaService jpa = getJpa();
		if (jpa == null) {
			context.println("jpa service not ready");
			return;
		}

		SyslogServerRegistry syslogServerRegistry = getSyslogServerRegistry();
		if (syslogServerRegistry == null) {
			context.println("syslog service not ready");
			return;
		}

		FilterManager fm = getFilterManager();
		if (fm == null) {
			context.println("filter service not ready");
			return;
		}

		// load jpa model
		if (jpa.getEntityManagerFactory("sleep-proxy") == null) {
			Properties props = new Properties();

			try {
				jpa.registerEntityManagerFactory("sleep-proxy", props, bc.getBundle().getBundleId());
				context.println("sleep proxy model loaded");
			} catch (BundleException e) {
				context.println(e.getMessage());
				return;
			}
		}

		// load syslog listener
		if (!syslogServerRegistry.contains("sleep-syslog")) {
			fm.loadFilter("org.krakenapps.syslog.SyslogReceiver", "sleep-syslog");
			fm.setProperty("sleep-syslog", "address", "0.0.0.0");
			fm.setProperty("sleep-syslog", "port", "514");
			fm.setProperty("sleep-syslog", "charset", "utf-8");
			fm.runFilter("sleep-syslog", 1000);
			context.println("sleep syslog server loaded");
		}
	}

	public void unload(String[] args) {
		// unload syslog listener
		FilterManager fm = getFilterManager();
		if (fm != null && fm.getFilter("sleep-syslog") != null) {
			fm.unloadFilter("sleep-syslog");
			context.println("sleep syslog server unloaded");
		}

		// unload jpa model
		JpaService jpa = getJpa();
		if (jpa != null) {
			if (jpa.getEntityManagerFactory("sleep-proxy") != null) {
				jpa.unregisterEntityManagerFactory("sleep-proxy");
				context.println("sleep proxy model unloaded");
			}
		}
	}

	private SleepProxyApi getSleepProxyApi() {
		return (SleepProxyApi) getService(SleepProxyApi.class.getName());
	}

	private JpaService getJpa() {
		return (JpaService) getService(JpaService.class.getName());
	}

	private ConfigStore getConfigStore() {
		return (ConfigStore) getService(ConfigStore.class.getName());
	}

	private SyslogServerRegistry getSyslogServerRegistry() {
		return (SyslogServerRegistry) getService(SyslogServerRegistry.class.getName());
	}

	private FilterManager getFilterManager() {
		return (FilterManager) getService(FilterManager.class.getName());
	}

	private TrafficMonitor getTrafficMonitor() {
		return (TrafficMonitor) getService(TrafficMonitor.class.getName());
	}

	private Object getService(String className) {
		ServiceReference ref = bc.getServiceReference(className);
		if (ref == null)
			return null;

		return bc.getService(ref);
	}
}
