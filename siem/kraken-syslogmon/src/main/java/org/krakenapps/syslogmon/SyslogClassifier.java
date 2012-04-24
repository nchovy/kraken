package org.krakenapps.syslogmon;

import org.krakenapps.syslog.Syslog;

/**
 * Syslog classifier identifies original log sources from one remote syslog
 * source and support multi-tenancy. By default, syslog source is identified by
 * remote address and syslog facility. However, some log server relays syslog
 * from multiple sources. In most case, syslog contains hostname.
 * SyslogClassifier extracts identifier for virtual syslog logger.
 * 
 * @author xeraph@nchovy.com
 * @since 1.1.0
 * 
 */
public interface SyslogClassifier {
	/**
	 * @return the classified identifier. e.g. hostname
	 */
	String classify(Syslog syslog);
}
