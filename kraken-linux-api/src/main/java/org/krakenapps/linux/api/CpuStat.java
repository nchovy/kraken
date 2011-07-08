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
package org.krakenapps.linux.api;

import java.io.IOException;

public class CpuStat {
	private long user;
	private long nice;
	private long system;
	private long idle;
	private long iowait;
	private long irq;
	private long softIrq;
	private long steal;
	private long guest;

	public long getUser() {
		return user;
	}

	public long getNice() {
		return nice;
	}

	public long getSystem() {
		return system;
	}

	public long getIdle() {
		return idle;
	}

	public long getIowait() {
		return iowait;
	}

	public long getIrq() {
		return irq;
	}

	public long getSoftIrq() {
		return softIrq;
	}

	public long getSteal() {
		return steal;
	}

	public long getGuest() {
		return guest;
	}

	public static CpuStat parse(String str) {
		CpuStat cpu = new CpuStat();
		String[] splitted = str.split(" ");

		if (splitted.length < 7)
			return null;

		cpu.user = Long.parseLong(splitted[0]);
		cpu.nice = Long.parseLong(splitted[1]);
		cpu.system = Long.parseLong(splitted[2]);
		cpu.idle = Long.parseLong(splitted[3]);
		cpu.iowait = Long.parseLong(splitted[4]);
		cpu.irq = Long.parseLong(splitted[5]);
		cpu.softIrq = Long.parseLong(splitted[6]);

		if (splitted.length >= 9) {
			cpu.steal = Long.parseLong(splitted[7]);
			cpu.guest = Long.parseLong(splitted[8]);
		}

		return cpu;
	}

	public static CpuUsage getCpuUsage() throws InterruptedException, IOException {
		return getCpuUsage(200, null);
	}

	public static CpuUsage getCpuUsage(int interval) throws InterruptedException, IOException {
		return getCpuUsage(interval, null);
	}

	public static CpuUsage getCpuUsage(int interval, Integer core) throws InterruptedException, IOException {
		CpuStat first = core == null ? KernelStat.getKernelStat().getCpu() : KernelStat.getKernelStat().getCpus().get(
				core);
		Thread.sleep(interval);
		CpuStat second = core == null ? KernelStat.getKernelStat().getCpu() : KernelStat.getKernelStat().getCpus().get(
				core);

		long deltaUser = second.getUser() - first.getUser();
		long deltaNice = second.getNice() - first.getNice();
		long deltaSystem = second.getSystem() - first.getSystem();
		long deltaIdle = second.getIdle() - first.getIdle();

		return new CpuUsage(deltaUser, deltaNice, deltaSystem, deltaIdle);
	}

}
