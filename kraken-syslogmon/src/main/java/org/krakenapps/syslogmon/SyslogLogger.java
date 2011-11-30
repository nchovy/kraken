package org.krakenapps.syslogmon;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.log.api.AbstractLogger;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerSpecification;
import org.krakenapps.log.api.SimpleLog;
import org.krakenapps.syslog.Syslog;

public class SyslogLogger extends AbstractLogger {

	public SyslogLogger(LoggerSpecification spec, LoggerFactory loggerFactory) {
		super(spec.getNamespace(), spec.getName(), spec.getDescription(), loggerFactory);
	}

	@Override
	protected void runOnce() {
	}

	public void push(Syslog syslog) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("facility", syslog.getFacility());
		m.put("severity", syslog.getSeverity());
		m.put("line", syslog.getMessage());
		write(new SimpleLog(new Date(), getFullName(), m));
	}
}
