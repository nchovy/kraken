package org.krakenapps.syslogmon;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
	 * syslog facility to logger mappings
	 */
	private ConcurrentMap<SyslogFacility, SyslogLogger> loggerMappings;
	private List<LoggerConfigOption> configs;

	@Requires
	private SyslogServerRegistry syslogRegistry;

	@Validate
	public void start() {
		loggerMappings = new ConcurrentHashMap<SyslogFacility, SyslogLogger>();

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
			int facility = Integer.valueOf(spec.getConfig().getProperty("facility"));
			SyslogFacility sf = new SyslogFacility(remote, facility);
			SyslogLogger logger = new SyslogLogger(spec, this);

			Logger old = loggerMappings.putIfAbsent(sf, logger);
			if (old != null)
				throw new IllegalStateException("same syslog mapping already exists for " + old);

			return logger;
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("unknown remote_ip", e);
		}
	}

	@Override
	public void deleteLogger(String namespace, String name) {
		SyslogFacility target = null;
		String targetName = namespace + "\\" + name;
		for (SyslogFacility sf : loggerMappings.keySet()) {
			Logger logger = loggerMappings.get(sf);
			if (logger.getFullName().equals(targetName))
				target = sf;
		}

		if (target != null)
			loggerMappings.remove(target);

		// do post clean job
		super.deleteLogger(namespace, name);
	}

	@Override
	public void onReceive(Syslog syslog) {
		SyslogFacility sf = new SyslogFacility(syslog.getRemoteAddress().getAddress(), syslog.getFacility());

		SyslogLogger logger = loggerMappings.get(sf);
		if (logger == null)
			return;

		logger.push(syslog);
	}

	private static class SyslogFacility {
		private InetAddress remote;
		private int facility;

		public SyslogFacility(InetAddress remote, int facility) {
			this.remote = remote;
			this.facility = facility;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + facility;
			result = prime * result + ((remote == null) ? 0 : remote.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SyslogFacility other = (SyslogFacility) obj;
			if (facility != other.facility)
				return false;
			if (remote == null) {
				if (other.remote != null)
					return false;
			} else if (!remote.equals(other.remote))
				return false;
			return true;
		}
	}
}
