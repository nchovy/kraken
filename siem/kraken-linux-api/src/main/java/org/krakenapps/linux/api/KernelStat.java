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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class KernelStat {
	private KernelStat() {
	}

	private CpuStat cpu;
	private List<CpuStat> cpus;
	private long pagedIn;
	private long pagedOut;
	private long swapIn;
	private long swapOut;
	private long totalInterrupts;
	private List<Long> particularInterrupts;
	private long context;
	private long bootTime; // in seconds since 1970-01-01 00:00:00 +0000(UTC).
	private int processes;// Number of forks since boot.
	private int runningProcess;
	private int blockedProcess;

	public CpuStat getCpu() {
		return cpu;
	}

	public List<CpuStat> getCpus() {
		return cpus;
	}

	public long getPagedIn() {
		return pagedIn;
	}

	public long getPagedOut() {
		return pagedOut;
	}

	public long getSwapIn() {
		return swapIn;
	}

	public long getSwapOut() {
		return swapOut;
	}

	public long getTotalInterrupts() {
		return totalInterrupts;
	}

	public List<Long> getParticularInterrupts() {
		return particularInterrupts;
	}

	public long getContext() {
		return context;
	}

	public long getBootTime() {
		return bootTime;
	}

	public int getProcesses() {
		return processes;
	}

	public int getRunningProcess() {
		return runningProcess;
	}

	public int getBlockedProcess() {
		return blockedProcess;
	}

	public static KernelStat getKernelStat() throws IOException {
		KernelStat stat = new KernelStat();
		BufferedReader br = null;
		FileInputStream is = null;

		try {
			is = new FileInputStream(new File("/proc/stat"));
			br = new BufferedReader(new InputStreamReader(is));
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;

				parse(stat, line);
			}

			return stat;
		} finally {
			if (is != null)
				is.close();
			if (br != null)
				br.close();
		}

	}

	private static void parse(KernelStat stat, String line) {
		int colon = line.indexOf(" ");
		String name = line.substring(0, colon).trim();
		String value = line.substring(colon + 1).trim();

		if (name.equals("cpu"))
			stat.cpu = CpuStat.parse(value);
		else if (name.startsWith("cpu")) {
			if (stat.cpus == null)
				stat.cpus = new ArrayList<CpuStat>();
			stat.cpus.add(CpuStat.parse(value));
		} else if (name.equals("page")) {
			String[] splitted = value.split(" ");
			stat.pagedIn = Long.parseLong(splitted[0]);
			stat.pagedOut = Long.parseLong(splitted[1]);
		} else if (name.equals("swap")) {
			String[] splitted = value.split(" ");
			stat.swapIn = Long.parseLong(splitted[0]);
			stat.swapOut = Long.parseLong(splitted[1]);
		} else if (name.equals("intr")) {
			String[] splitted = value.split(" ");
			stat.totalInterrupts = Long.parseLong(splitted[0]);
			stat.particularInterrupts = new ArrayList<Long>();
			for (int i = 1; i < splitted.length; i++) {
				long intr = Long.parseLong(splitted[i]);
				stat.particularInterrupts.add(intr);
			}
		} else if (name.equals("ctxt"))
			stat.context = Long.parseLong(value);
		else if (name.equals("btime"))
			stat.bootTime = Long.parseLong(value);
		else if (name.equals("processes"))
			stat.processes = Integer.parseInt(value);
		else if (name.equals("procs_running"))
			stat.runningProcess = Integer.parseInt(value);
		else if (name.equals("procs_blocked"))
			stat.blockedProcess = Integer.parseInt(value);
	}
}
