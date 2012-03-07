package org.krakenapps.syslogmon;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.log.api.AbstractLoggerFactory;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.log.api.LoggerSpecification;
import org.krakenapps.log.api.StringConfigType;
import org.krakenapps.syslog.Syslog;
import org.krakenapps.syslog.SyslogListener;
import org.krakenapps.syslog.SyslogServerRegistry;

@Component(name = "syslog-logger-factory")
@Provides
public class SyslogLoggerFactory extends AbstractLoggerFactory implements SyslogListener {

	/**
	 * remote ip to logger mappings
	 */
	private ConcurrentMap<InetAddress, SyslogLogger> loggerMappings;
	private List<LoggerConfigOption> configs;

	@Requires
	private SyslogServerRegistry syslogRegistry;

	@Validate
	public void start() {
		loggerMappings = new ConcurrentHashMap<InetAddress, SyslogLogger>();

		prepareConfigOptions();
		syslogRegistry.addSyslogListener(this);
	}

	@Invalidate
	public void stop() {
		if (syslogRegistry != null)
			syslogRegistry.removeSyslogListener(this);
	}

	private void prepareConfigOptions() {
		configs = new ArrayList<LoggerConfigOption>();
		{
			Map<Locale, String> names = new HashMap<Locale, String>();
			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			names.put(Locale.ENGLISH, "remote ip");
			descriptions.put(Locale.ENGLISH, "remote syslog sender's ip address");
			configs.add(new StringConfigType("remote_ip", names, descriptions, true));
		}
		{
			Map<Locale, String> names = new HashMap<Locale, String>();
			Map<Locale, String> descriptions = new HashMap<Locale, String>();
			names.put(Locale.ENGLISH, "syslog facility");
			descriptions.put(Locale.ENGLISH, "syslog facility number");
			configs.add(new StringConfigType("facility", names, descriptions, true));
		}
	}

	@Override
	public String getName() {
		return "syslog";
	}

	@Override
	public String getDisplayName(Locale locale) {
		return "syslog logger";
	}

	@Override
	public String getDescription(Locale locale) {
		return "syslog logger";
	}

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		return configs;
	}

	@Override
	protected Logger createLogger(LoggerSpecification spec) {
		try {
			InetAddress remote = InetAddress.getByName(spec.getConfig().getProperty("remote_ip"));
			SyslogLogger logger = new SyslogLogger(spec, this);
			String facility = spec.getConfig().getProperty("facility");
			if (facility == null)
				throw new IllegalArgumentException("syslog facility is required");

			logger.setFacilities(parseFacilities(facility));

			Logger old = loggerMappings.putIfAbsent(remote, logger);
			if (old != null)
				throw new IllegalStateException("same syslog mapping already exists for " + old);

			return logger;
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("unknown remote_ip", e);
		}
	}

	private Set<Integer> parseFacilities(String facility) {
		Set<Integer> facilities = new HashSet<Integer>();
		String[] tokens = facility.split(",");
		for (String token : tokens)
			facilities.add(Integer.valueOf(token.trim()));
		return facilities;
	}

	@Override
	public void deleteLogger(String namespace, String name) {
		InetAddress target = null;
		String targetName = namespace + "\\" + name;
		for (InetAddress remote : loggerMappings.keySet()) {
			Logger logger = loggerMappings.get(remote);
			if (logger.getFullName().equals(targetName))
				target = remote;
		}

		if (target != null)
			loggerMappings.remove(target);

		// do post clean job
		super.deleteLogger(namespace, name);
	}

	@Override
	public void onReceive(Syslog syslog) {
		SyslogLogger logger = loggerMappings.get(syslog.getRemoteAddress().getAddress());
		if (logger == null)
			return;

		logger.push(syslog);
	}
}
