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

public class Process {
	private Process() {
	}

	// add cpu usage
	private int pid;
	private String name;
	private String cmdLine;
	private int ppid;
	private String state;
	private int fdSize;
	private int vmPeak; // kB
	private int vmSize; // kB
	private int vmLck; // kB
	private int vmHwm; // kB
	private int vmRss; // kB
	private int vmData; // kB
	private int vmStk; // kB
	private int vmExe; // kB
	private int vmLib; // kB
	private int vmPte; // kB
	private int threads;

	public int getPid() {
		return pid;
	}

	public String getName() {
		return name;
	}

	public String getCmdLine() {
		return cmdLine;
	}

	public int getPpid() {
		return ppid;
	}

	public String getState() {
		return state;
	}

	public int getFdSize() {
		return fdSize;
	}

	public int getVmPeak() {
		return vmPeak;
	}

	public int getVmSize() {
		return vmSize;
	}

	public int getVmLck() {
		return vmLck;
	}

	public int getVmHwm() {
		return vmHwm;
	}

	public int getVmRss() {
		return vmRss;
	}

	public int getVmData() {
		return vmData;
	}

	public int getVmStk() {
		return vmStk;
	}

	public int getVmExe() {
		return vmExe;
	}

	public int getVmLib() {
		return vmLib;
	}

	public int getVmPte() {
		return vmPte;
	}

	public int getThreads() {
		return threads;
	}

	public static List<Process> getProcesses() {
		List<Process> processes = new ArrayList<Process>();

		for (File f : new File("/proc").listFiles()) {
			if (!f.isDirectory())
				continue;

			try {
				// if directory name is not integer (pid), it will throw
				// NumberFormatException.
				Integer.valueOf(f.getName());

				Process p = new Process();
				readStatus(f, p);
				readCmdLine(f, p);

				processes.add(p);
			} catch (Exception e) {
				// ignore
			}
		}

		return processes;
	}

	private static void readCmdLine(File f, Process p) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(f, "cmdline"))));
			p.cmdLine = br.readLine();
		} catch (IOException e) {
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
		}
	}

	private static void readStatus(File f, Process p) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(f, "status"))));

			while (true) {
				String line = br.readLine();
				if (line == null)
					break;

				parse(p, line);
			}
		} finally {
			if (br != null)
				br.close();
		}
	}

	private static void parse(Process p, String line) {
		int colon = line.indexOf(':');
		String name = line.substring(0, colon).trim();
		String value = line.substring(colon + 1).trim();

		if (name.equals("Name"))
			p.name = value;
		else if (name.equals("State"))
			p.state = value.split(" ")[0].trim();
		else if (name.equals("Pid"))
			p.pid = Integer.valueOf(value);
		else if (name.equals("PPid"))
			p.ppid = Integer.valueOf(value);
		else if (name.equals("VmPeak"))
			p.vmPeak = Integer.valueOf(value.split(" ")[0]);
		else if (name.equals("VmSize"))
			p.vmSize = Integer.valueOf(value.split(" ")[0]);
		else if (name.equals("VmLck"))
			p.vmLck = Integer.valueOf(value.split(" ")[0]);
		else if (name.equals("VmHWM"))
			p.vmHwm = Integer.valueOf(value.split(" ")[0]);
		else if (name.equals("VmRSS"))
			p.vmRss = Integer.valueOf(value.split(" ")[0]);
		else if (name.equals("VmData"))
			p.vmData = Integer.valueOf(value.split(" ")[0]);
		else if (name.equals("VmStk"))
			p.vmStk = Integer.valueOf(value.split(" ")[0]);
		else if (name.equals("VmExe"))
			p.vmExe = Integer.valueOf(value.split(" ")[0]);
		else if (name.equals("VmLib"))
			p.vmLib = Integer.valueOf(value.split(" ")[0]);
		else if (name.equals("VmPTE"))
			p.vmPte = Integer.valueOf(value.split(" ")[0]);
		else if (name.equals("Threads"))
			p.threads = Integer.valueOf(value);
	}

	@Override
	public String toString() {
		return String.format("[%s] %s, mem=%dKB", pid, name, vmSize);
	}

}
