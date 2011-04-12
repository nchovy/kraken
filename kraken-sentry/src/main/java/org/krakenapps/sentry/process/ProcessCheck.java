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
package org.krakenapps.sentry.process;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class ProcessCheck {
	private String processName;
	private ProcessCheckOption option;
	private Date lastCheck;

	/**
	 * True if it was running
	 */
	private boolean lastStatus;

	public static void run(ProcessMonitor processMonitor, Set<String> processNames) {
		for (ProcessCheck check : processMonitor.getProcessChecklist()) {
			String target = check.getProcessName().toLowerCase();

			// set only if process status changed
			boolean isRunning = processNames.contains(target);
			check.setLastCheck(new Date());
			if (check.getLastStatus() != isRunning) {
				check.setLastStatus(isRunning);
				processMonitor.dispatch(target, check.getOption(), isRunning);
			}
		}
	}

	public ProcessCheck(String processName, ProcessCheckOption option) {
		this.processName = processName;
		this.option = option;
	}

	public String getProcessName() {
		return processName;
	}

	public ProcessCheckOption getOption() {
		return option;
	}

	public Date getLastCheck() {
		return lastCheck;
	}

	public void setLastCheck(Date lastCheck) {
		this.lastCheck = lastCheck;
	}

	public boolean getLastStatus() {
		return lastStatus;
	}

	public void setLastStatus(boolean lastStatus) {
		this.lastStatus = lastStatus;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return String.format("process=%s, check option=%s, last check=%s, last running status=%s", processName,
				option.toString().toLowerCase(), lastCheck == null ? null : dateFormat.format(lastCheck), lastStatus);
	}
}