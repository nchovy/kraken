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
package org.krakenapps.sentry.windows.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.sentry.process.ProcessCheck;
import org.krakenapps.sentry.process.ProcessChecker;
import org.krakenapps.sentry.process.ProcessMonitor;
import org.krakenapps.winapi.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "windows-process-checker")
@Provides
public class WindowsProcessChecker implements ProcessChecker {
	private final Logger logger = LoggerFactory.getLogger(WindowsProcessChecker.class.getName());

	@Requires
	private ProcessMonitor processMonitor;

	@Override
	public void run() {
		logger.trace("kraken windows sentry: starting periodic process check");

		Set<String> processSet = new HashSet<String>();
		for (Process p : Process.getProcesses()) {
			if (p != null)
				processSet.add(p.getName().toLowerCase());
		}

		ProcessCheck.run(processMonitor, processSet);
	}

	@Override
	public ProcessMonitor getProcessMonitor() {
		return processMonitor;
	}
}
