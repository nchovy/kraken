/*
 * Copyright 2011 Future Systems
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
