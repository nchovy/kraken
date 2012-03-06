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
package org.krakenapps.sentry.windows.logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.log.api.AbstractLogger;
import org.krakenapps.log.api.Log;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerSpecification;
import org.krakenapps.log.api.SimpleLog;
import org.krakenapps.winapi.MemoryStatus;

public class MemoryUsageLogger extends AbstractLogger {
	private static final String MEMORY_LOG_TEMPLATE = "memory usage: physical %d/%d, virtual %d/%d";

	public MemoryUsageLogger(LoggerSpecification spec, LoggerFactory loggerFactory) {
		super(spec.getName(), spec.getDescription(), loggerFactory);
	}

	@Override
	protected void runOnce() {
		MemoryStatus ms = new MemoryStatus();
		Map<String, Object> m = new HashMap<String, Object>();

		long phyFree = ms.getAvailablePhysical();
		long phyTotal = ms.getTotalPhysical();
		long virtualFree = ms.getAvailableVirtual();
		long virtualTotal = ms.getTotalVirtual();

		m.put("pfree", phyFree);
		m.put("ptotal", phyTotal);
		m.put("vfree", virtualFree);
		m.put("vtotal", virtualTotal);

		String msg = String.format(MEMORY_LOG_TEMPLATE, phyFree, phyTotal, virtualFree, virtualTotal);
		Log log = new SimpleLog(new Date(), getFullName(), "system", msg, m);

		write(log);
	}
}
