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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.winapi.impl.LongRef;
import org.krakenapps.winapi.impl.ObjectRef;

public class Process {
	public static final int PROCESS_VM_READ = 0x0010;
	public static final int PROCESS_ALL_ACCESS = 0x001F0FFF;
	public static final int PROCESS_QUERY_INFORMATION = 0x400;
	public static final int PROCESS_QUERY_LIMITED_INFORMATION = 0x1000;

	static {
		System.loadLibrary("winapi");
	}

	public static int getProcessorCount() {
		return GetProcessorCount();
	}

	/**
	 * Calculates cpu usages of all processes.
	 * 
	 * @param interval
	 *            interval in milliseconds
	 * @return the pid, cpu usage (percent) pairs
	 * @throws InterruptedException
	 *             when thread is interrupted
	 */
	public static Map<Integer, Integer> getCpuUsages(int interval) throws InterruptedException {
		Map<Integer, Long> first = new HashMap<Integer, Long>();
		Map<Integer, Long> second = new HashMap<Integer, Long>();
		Map<Integer, Integer> usages = new HashMap<Integer, Integer>();

		for (Process p : Process.getProcesses()) {
			first.put(p.getPid(), p.getPrivilegedProcessorTime() + p.getUserProcessorTime());
		}
		long begin = new Date().getTime();

		Thread.sleep(interval);

		for (Process p : Process.getProcesses()) {
			second.put(p.getPid(), p.getPrivilegedProcessorTime() + p.getUserProcessorTime());
		}
		long end = new Date().getTime();

		int cpuCount = getProcessorCount();
		long total = end - begin;

		for (int pid : second.keySet()) {
			if (!first.containsKey(pid))
				continue;

			// 100ns -> ms (1000000 ns)
			long use = (second.get(pid) - first.get(pid));
			int usage = (int) (use / total / 100 / cpuCount);

			usages.put(pid, usage);
		}

		return usages;
	}

	private int pid;
	private String name;
	private long privilegedProcessorTime;
	private long userProcessorTime;
	private ProcessMemoryCounters memCounters = new ProcessMemoryCounters();

	private Process(int pid, String name) {
		this.pid = pid;
		this.name = name;

		refresh();
	}

	public int getPid() {
		return pid;
	}

	public String getName() {
		return name;
	}

	public long getPrivilegedProcessorTime() {
		return privilegedProcessorTime;
	}

	public long getUserProcessorTime() {
		return userProcessorTime;
	}

	public long getWorkingSet() {
		return memCounters.getWorkingSetSize();
	}

	public long getPagedMemorySize() {
		return memCounters.getQuotaPagedPoolUsage();
	}

	public long getNonPagedMemorySize() {
		return memCounters.getQuotaNonPagedPoolUsage();
	}

	public long getPageFault() {
		return memCounters.getPageFaultCount();
	}

	public long getPrivateWorkingSet() {
		return memCounters.getPrivateUsage();
	}

	@Override
	public String toString() {
		return "pid=" + pid + ", name=" + name;
	}

	public void refresh() {
		int hProcess = OpenProcess(PROCESS_VM_READ | PROCESS_QUERY_INFORMATION, 0, pid);
		if (hProcess == 0)
			return;

		LongRef creation = new LongRef();
		LongRef exit = new LongRef();
		LongRef kernel = new LongRef();
		LongRef user = new LongRef();
		int ret = GetProcessTimes(hProcess, creation, exit, kernel, user);
		if (ret == 1) {
			privilegedProcessorTime = kernel.value;
			userProcessorTime = user.value;
		}

		memCounters = getMemoryCounters(hProcess);

		CloseHandle(hProcess);
	}

	private static Integer[] EnumProcesses() {
		ObjectRef ref = new ObjectRef();
		EnumProcesses(ref);
		Integer[] array = (Integer[]) ref.value;
		return array;
	}

	public static List<Process> getProcesses() {
		List<Process> processes = new ArrayList<Process>();

		Integer[] array = EnumProcesses();
		for (int pid : array) {
			int hProcess = OpenProcess(PROCESS_VM_READ | PROCESS_QUERY_INFORMATION, 0, pid);
			if (hProcess != 0) {
				String baseName = GetModuleBaseName(hProcess, 1000);
				if (baseName == null)
					baseName = "";
				processes.add(new Process(pid, baseName));
			} else {
				String n = "";
				if (pid == 0)
					n = "System Idle Process";
				else if (pid == 4)
					n = "System";
				else
					continue;

				processes.add(new Process(pid, n));
			}

			CloseHandle(hProcess);
		}

		return processes;
	}

	private ProcessMemoryCounters getMemoryCounters(int hProcess) {
		LongRef pageFaultCount = new LongRef();
		LongRef peakWorkingSetSize = new LongRef();
		LongRef workingSetSize = new LongRef();
		LongRef quotaPeakPagedPoolUsage = new LongRef();
		LongRef quotaPagedPoolUsage = new LongRef();
		LongRef quotaPeakNonPagedPoolUsage = new LongRef();
		LongRef quotaNonPagedPoolUsage = new LongRef();
		LongRef pagefileUsage = new LongRef();
		LongRef peakPagefileUsage = new LongRef();
		LongRef privateUsage = new LongRef();

		GetProcessMemoryInfo(hProcess, pageFaultCount, peakWorkingSetSize, workingSetSize, quotaPeakPagedPoolUsage,
				quotaPagedPoolUsage, quotaPeakNonPagedPoolUsage, quotaNonPagedPoolUsage, pagefileUsage,
				peakPagefileUsage, privateUsage);

		return new ProcessMemoryCounters(pageFaultCount.value, peakWorkingSetSize.value, workingSetSize.value,
				quotaPeakPagedPoolUsage.value, quotaPagedPoolUsage.value, quotaPeakNonPagedPoolUsage.value,
				quotaNonPagedPoolUsage.value, pagefileUsage.value, peakPagefileUsage.value, privateUsage.value);
	}

	private static native int EnumProcesses(ObjectRef pids);

	private static native int OpenProcess(int desiredAccess, int inheritHandle, int processId);

	private static native String GetModuleBaseName(int hProcess, int maxLength);

	private static native int CloseHandle(int handle);

	private static native int GetProcessTimes(int hProcess, LongRef creation, LongRef exit, LongRef kernel, LongRef user);

	private static native int GetProcessorCount();

	private static native int GetProcessMemoryInfo(int hProcess, LongRef pageFaultCount, LongRef peakWorkingSetSize,
			LongRef workingSetSize, LongRef quotaPeakPagedPoolUsage, LongRef quotaPagedPoolUsage,
			LongRef quotaPeakNonPagedPoolUsage, LongRef quotaNonPagedPoolUsage, LongRef pagefileUsage,
			LongRef peakPagefileUsage, LongRef privateUsage);

}
