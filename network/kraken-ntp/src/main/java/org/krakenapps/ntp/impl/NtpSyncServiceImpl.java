package org.krakenapps.ntp.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.cron.CronService;
import org.krakenapps.cron.Schedule;
import org.krakenapps.ntp.NtpClient;
import org.krakenapps.ntp.NtpSyncService;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "ntp-sync-service")
@Provides
public class NtpSyncServiceImpl implements NtpSyncService {
	private Logger logger = LoggerFactory.getLogger(NtpSyncServiceImpl.class);

	@Requires
	private PreferencesService prefsvc;

	@Requires
	private CronService cronService;

	@ServiceProperty(name = "instance.name")
	private String instanceName;

	private NtpClient client;
	private String cronExp = "*/10 * * * *";

	public NtpSyncServiceImpl() {
	}

	private void ensureClient() {
		if (client != null)
			return;

		try {
			Preferences p = getPreference();
			String host = p.get("server", null);
			int timeout = p.getInt("timeout", 5000);
			this.client = new NtpClient(host, timeout);
		} catch (UnknownHostException e) {
			logger.error("kraken ntp: failed to create ntp client", e);
		}
	}

	@Override
	public NtpClient getNtpClient() {
		return client;
	}

	@Override
	public void setNtpClient(NtpClient client) {
		this.client = client;
	}

	@Override
	public InetAddress getTimeServer() {
		ensureClient();
		return this.client.getTimeServer();
	}

	@Override
	public void setTimeServer(InetAddress timeServer) {
		ensureClient();
		this.client.setTimeServer(timeServer);
		Preferences p = getPreference();
		p.put("server", timeServer.getHostName());
		syncPreferences(p);
	}

	@Override
	public int getTimeout() {
		return this.client.getTimeout();
	}

	@Override
	public void setTimeout(int timeout) {
		ensureClient();
		this.client.setTimeout(timeout);
		Preferences p = getPreference();
		p.putInt("timeout", timeout);
		syncPreferences(p);
	}

	private Preferences getPreference() {
		return prefsvc.getSystemPreferences().node("ntp");
	}

	private void syncPreferences(Preferences p) {
		try {
			p.flush();
			p.sync();
		} catch (BackingStoreException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	@Override
	public String getSchedule() {
		return cronExp;
	}

	@Override
	public void setSchedule(String exp) {
		this.cronExp = exp;
	}

	@Override
	public void run() {
		try {
			if (client != null)
				client.sync();
		} catch (Throwable t) {
			logger.error("kraken ntp: cannot sync time", t);
		}
	}

	@Override
	@Validate
	public void start() {
		Integer cronJobId = getCronJobId();
		if (cronJobId == null) {
			try {
				Schedule schedule = new Schedule.Builder(instanceName).build(cronExp);
				cronService.registerSchedule(schedule);
			} catch (Exception e) {
				logger.error("kraken ntp: cron register failed");
			}
		}
	}

	@Override
	public void stop() {
		Integer cronJobId = getCronJobId();
		if (cronJobId != null)
			cronService.unregisterSchedule(cronJobId);
	}

	@Override
	public boolean isRunning() {
		return getCronJobId() != null;
	}

	private Integer getCronJobId() {
		Map<Integer, Schedule> schedules = cronService.getSchedules();
		for (Integer id : schedules.keySet()) {
			Schedule schedule = schedules.get(id);
			if (schedule.getTaskName().equals(instanceName))
				return id;
		}
		return null;
	}
}
