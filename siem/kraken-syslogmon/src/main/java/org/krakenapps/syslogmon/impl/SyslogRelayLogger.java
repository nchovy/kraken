package org.krakenapps.syslogmon.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.log.api.AbstractLogger;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerSpecification;
import org.krakenapps.log.api.SimpleLog;
import org.krakenapps.syslog.Syslog;

/**
 * syslog relay logger to support multi-tenancy.
 * 
 * @author xeraph@nchovy.com
 * 
 */
public class SyslogRelayLogger extends AbstractLogger {
	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(SyslogRelayLogger.class.getName());

	public SyslogRelayLogger(LoggerSpecification spec, LoggerFactory loggerFactory) {
		super(spec.getNamespace(), spec.getName(), spec.getDescription(), loggerFactory, spec.getConfig());
	}

	@Override
	protected void runOnce() {
	}

	/**
	 * Pass syslog through log pipeline
	 */
	public void push(Syslog syslog) {
		if (slog.isDebugEnabled())
			slog.debug("kraken syslogmon: passed to syslog virtual logger [{}]", syslog);

		if (isRunning()) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("facility", syslog.getFacility());
			m.put("severity", syslog.getSeverity());
			m.put("line", syslog.getMessage());
			write(new SimpleLog(new Date(), getFullName(), m));
		}
	}
}
