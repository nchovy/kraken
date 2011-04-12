/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.filter;

import org.krakenapps.filter.exception.ConfigurationException;

/**
 * ActiveFilter class should be implemented by any filter class which filters
 * are intended to be excuted by a thread.
 * 
 * @author xeraph
 * @since 1.0.0
 * @see Filter
 */
public abstract class ActiveFilter extends DefaultFilter {
	private boolean isRunning;

	/**
	 * Returns true if thread is running.
	 * 
	 * @return true if thread is running
	 */
	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	/**
	 * Initialize callback. ActiveFilterRunner calls this method before the run
	 * loop. Override this.
	 * 
	 * @throws ConfigurationException
	 *             if failed to configure
	 */
	public void open() throws ConfigurationException {
	}

	/**
	 * Finalize callback. ActiveFilterRunner calls this method after the run
	 * loop. Override this.
	 */
	public void close() {
	}

	/**
	 * ActiveFilterRunner calls this callback in each loop. Thread will sleep
	 * some milliseconds after run.
	 * 
	 * @throws InterruptedException
	 *             if thread is interrupted
	 */
	abstract public void run() throws InterruptedException;
}
