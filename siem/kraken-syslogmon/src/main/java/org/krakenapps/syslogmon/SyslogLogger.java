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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.krakenapps.log.api.AbstractLogger;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerSpecification;
import org.krakenapps.log.api.SimpleLog;
import org.krakenapps.syslog.Syslog;

public class SyslogLogger extends AbstractLogger {

	/**
	 * collect target facilities
	 */
	private Set<Integer> facilities = new HashSet<Integer>();

	public SyslogLogger(LoggerSpecification spec, LoggerFactory loggerFactory) {
		super(spec.getNamespace(), spec.getName(), spec.getDescription(), loggerFactory, spec.getConfig());
	}

	public Set<Integer> getFacilities() {
		return facilities;
	}

	public void setFacilities(Set<Integer> facilities) {
		this.facilities = facilities;
	}

	@Override
	protected void runOnce() {
	}

	public void push(Syslog syslog) {
		if (!facilities.contains(syslog.getFacility()))
			return;

		if (isRunning()) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("facility", syslog.getFacility());
			m.put("severity", syslog.getSeverity());
			m.put("line", syslog.getMessage());
			write(new SimpleLog(new Date(), getFullName(), m));
		}
	}
}
