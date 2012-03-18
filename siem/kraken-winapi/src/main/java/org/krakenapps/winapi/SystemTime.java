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
package org.krakenapps.winapi;

import java.util.Date;

public class SystemTime {
	private int idlePercent;
	private int kernelPercent;
	private int userPercent;

	static {
		System.loadLibrary("winapi");
	}

	public SystemTime() throws InterruptedException {
		this(100);
	}

	public SystemTime(int interval) throws InterruptedException {
		long[] first = getSystemTimes();
		long begin = new Date().getTime();
		Thread.sleep(interval);
		long[] second = getSystemTimes();
		long end = new Date().getTime();

		idlePercent = (int) ((second[0] - first[0]) / (end - begin) / 100 / Process.getProcessorCount());
		kernelPercent = (int) ((second[1] - first[1]) / (end - begin) / 100 / Process.getProcessorCount())
				- idlePercent;
		userPercent = (int) ((second[2] - first[2]) / (end - begin) / 100 / Process.getProcessorCount());

		idlePercent = 100 - (kernelPercent + userPercent);
		if (idlePercent < 0)
			idlePercent = 0;
	}

	private native long[] getSystemTimes();

	public int getUsage() {
		int usage = userPercent + kernelPercent;
		return usage > 100 ? 100 : usage;
	}

	public int getIdlePercent() {
		return idlePercent;
	}

	public int getKernelPercent() {
		return kernelPercent;
	}

	public int getUserPercent() {
		return userPercent;
	}

	@Override
	public String toString() {
		return "idle=" + idlePercent + ", kernel=" + kernelPercent + ", user=" + userPercent;
	}
}
