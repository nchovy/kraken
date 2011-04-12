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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.log.api.AbstractLogger;
import org.krakenapps.log.api.Log;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.SimpleLog;
import org.krakenapps.sentry.process.ProcessCheckEventListener;
import org.krakenapps.sentry.process.ProcessCheckOption;
import org.krakenapps.sentry.process.ProcessChecker;
import org.krakenapps.sentry.process.ProcessMonitor;

public class ProcessCheckLogger extends AbstractLogger implements ProcessCheckEventListener {
	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(ProcessCheckLogger.class.getName());
	private ProcessChecker checker;

	public ProcessCheckLogger(ProcessChecker checker, String namespace, String name, String description,
			LoggerFactory loggerFactory) {
		super(namespace, name, description, loggerFactory);
		this.checker = checker;
	}

	@Override
	protected void runOnce() {
		ProcessMonitor monitor = checker.getProcessMonitor();
		monitor.addListener(this);
		try {
			checker.run();
		} finally {
			monitor.removeListener(this);
		}
	}

	@Override
	public void onCheck(String processName, ProcessCheckOption option, boolean isRunning) {
		String msg = "";

		if (option == ProcessCheckOption.Allow && !isRunning)
			msg = String.format("process check: warning, [%s] is not running", processName);
		else if (option == ProcessCheckOption.Deny && isRunning)
			msg = String.format("process check: warning, [%s] is running", processName);
		else if (option == ProcessCheckOption.Allow && isRunning)
			msg = String.format("process check: ok, [%s] is running", processName);
		else if (option == ProcessCheckOption.Deny && !isRunning)
			msg = String.format("process check: ok, [%s] is not running", processName);

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", processName);
		m.put("policy", option.toString().toLowerCase());

		Log log = new SimpleLog(new Date(), getFullName(), "processcheck", msg, m);
		write(log);
		
		if (slog.isTraceEnabled())
			slog.trace("kraken windows sentry: process check log [" + log.toString() + "]");
	}
}
