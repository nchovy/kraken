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
package org.krakenapps.sentry.linux.logger;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.linux.api.CpuStat;
import org.krakenapps.linux.api.CpuUsage;
import org.krakenapps.log.api.AbstractLogger;
import org.krakenapps.log.api.Log;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.SimpleLog;

public class CpuUsageLogger extends AbstractLogger {
	public CpuUsageLogger(String namespace, String name, String description, LoggerFactory loggerFactory) {
		super(namespace, name, description, loggerFactory);
	}

	@Override
	protected void runOnce() {
		try {
			CpuUsage usage = CpuStat.getCpuUsage();

			Map<String, Object> m = new HashMap<String, Object>();
			m.put("idle", usage.getIdle());
			m.put("kernel", usage.getSystem());
			m.put("user", usage.getUser());

			String msg = String.format("cpu usage: %d%%", usage.getUsage());
			Log log = new SimpleLog(new Date(), getFullName(), "system", msg, m);
			write(log);
		} catch (IOException e) {
		} catch (InterruptedException e) {
		}
	}

}
