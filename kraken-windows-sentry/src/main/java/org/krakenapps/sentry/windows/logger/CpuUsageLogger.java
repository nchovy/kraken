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
import org.krakenapps.winapi.SystemTime;

public class CpuUsageLogger extends AbstractLogger {

	public CpuUsageLogger(LoggerSpecification spec, LoggerFactory loggerFactory) {
		super(spec.getNamespace(), spec.getName(), spec.getDescription(), loggerFactory);
	}

	@Override
	protected void runOnce() {
		try {
			SystemTime s = new SystemTime();

			Map<String, Object> m = new HashMap<String, Object>();
			m.put("idle", s.getIdlePercent());
			m.put("kernel", s.getKernelPercent());
			m.put("user", s.getUserPercent());

			String msg = String.format("cpu usage: %d%%", s.getUsage());
			Log log = new SimpleLog(new Date(), getFullName(), "system", msg, m);
			write(log);
		} catch (InterruptedException e) {
		}
	}

}
