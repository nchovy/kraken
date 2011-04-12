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
package org.krakenapps.sentry.logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.log.api.AbstractLogger;
import org.krakenapps.log.api.Log;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.SimpleLog;

public class DiskUsageLogger extends AbstractLogger {
	public DiskUsageLogger(String namespace, String name, String description, LoggerFactory loggerFactory) {
		super(name, description, loggerFactory);
	}

	@Override
	protected void runOnce() {
		File[] roots = File.listRoots();
		List<Log> logs = new ArrayList<Log>(roots.length);

		for (File f : roots) {
			long used = f.getTotalSpace() - f.getFreeSpace();

			int usage = 0;
			if (f.getTotalSpace() != 0)
				usage = (int) (used * 100 / f.getTotalSpace());

			String msg = String.format("disk usage: partition=%s, usage=%d%%, free=%dMB, total=%dMB", f.getPath(),
					usage, f.getFreeSpace() / 1048576, f.getTotalSpace() / 1048576);

			Map<String, Object> m = new HashMap<String, Object>();
			m.put("scope", "partition");
			m.put("partition", f.getPath());
			m.put("usage", usage);
			m.put("free", f.getFreeSpace());
			m.put("used", used);
			m.put("total", f.getTotalSpace());

			Log log = new SimpleLog(new Date(), getFullName(), "partition", msg, m);
			write(log);

			logs.add(log);
		}

		// find max
		Log max = findMax(logs);
		Map<String, Object> params = max.getParams();

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("scope", "total");
		m.put("max_partition", params.get("partition"));
		m.put("max_usage", params.get("usage"));

		String msg = String.format("disk usage: max partition usage [%s (%d%%)]", m.get("max_partition"),
				m.get("max_usage"));
		Log log = new SimpleLog(new Date(), getFullName(), "total", msg, m);
		write(log);
	}

	private Log findMax(List<Log> logs) {
		Log selected = null;
		int maxUsage = 0;

		for (Log log : logs) {
			Integer usage = (Integer) log.getParams().get("usage");
			if (selected == null || usage >= maxUsage) {
				selected = log;
				maxUsage = usage;
			}
		}

		return selected;
	}
}
