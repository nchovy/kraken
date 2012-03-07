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

import java.util.Collection;

public interface ProcessMonitor {
	Collection<ProcessCheck> getProcessChecklist();

	void addProcess(String processName, ProcessCheckOption option);

	void removeProcess(String processName);

	void addListener(ProcessCheckEventListener callback);

	void removeListener(ProcessCheckEventListener callback);

	/**
	 * This method should be called by process checker only.
	 */
	void dispatch(String processName, ProcessCheckOption option, boolean isRunning);
}
