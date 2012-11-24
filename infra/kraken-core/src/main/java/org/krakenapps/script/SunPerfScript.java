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
package org.krakenapps.script;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;

import org.krakenapps.api.PathAutoCompleter;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;

import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.UnixOperatingSystemMXBean;

@SuppressWarnings("restriction")
public class SunPerfScript implements Script {
	private ScriptContext context;

	@Override
	public void setScriptContext(ScriptContext arg0) {
		this.context = arg0;
	}

	@ScriptUsage(description = "dump .hprof file", arguments = {
			@ScriptArgument(name = "path", type = "string", description = "dump file path", autocompletion = PathAutoCompleter.class),
			@ScriptArgument(name = "live only", type = "string", description = "if true dump only live objects") })
	public void dumpHeap(String[] args) throws IOException {
		MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
		HotSpotDiagnosticMXBean bean = ManagementFactory.newPlatformMXBeanProxy(platformMBeanServer,
				"com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
		bean.dumpHeap(args[0], Boolean.parseBoolean(args[1]));
	}

	public void system(String[] args) throws IOException {
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		if (os instanceof com.sun.management.OperatingSystemMXBean) {
			com.sun.management.OperatingSystemMXBean sunbean = (com.sun.management.OperatingSystemMXBean) os;
			NumberFormat nf = NumberFormat.getNumberInstance();
			context.printf("Process cpu time: %s\n", nf.format(sunbean.getProcessCpuTime()));
			context.printf("Free phys memory: %s/%s\n", nf.format(sunbean.getFreePhysicalMemorySize()),
					nf.format(sunbean.getTotalPhysicalMemorySize()));
			context.printf("Free swap space: %s/%s\n", nf.format(sunbean.getFreeSwapSpaceSize()),
					nf.format(sunbean.getTotalSwapSpaceSize()));
			context.printf("Commited virtual memory: %s\n", nf.format(sunbean.getCommittedVirtualMemorySize()));
		}
		if (os instanceof UnixOperatingSystemMXBean) {
			UnixOperatingSystemMXBean unixbean = (UnixOperatingSystemMXBean) os;
			context.printf("open fd: %s/%s\n", unixbean.getOpenFileDescriptorCount(), unixbean.getMaxFileDescriptorCount());
		}
	}

	public void topThreads(String[] args) throws InterruptedException {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();

		if (!bean.isThreadCpuTimeSupported()) {
			context.println("thread cpu time is not supported.");
			return;
		}

		if (!bean.isThreadCpuTimeEnabled()) {
			context.println("thread cpu time is not enabled.");
			return;
		}

		ArrayList<ThreadCpuUsage> usages = new ArrayList<ThreadCpuUsage>();

		for (long tid : bean.getAllThreadIds()) {
			long time = bean.getThreadCpuTime(tid);
			usages.add(new ThreadCpuUsage(tid, time));
		}

		Thread.sleep(200);

		for (long tid : bean.getAllThreadIds()) {
			ThreadCpuUsage usage = find(usages, tid);
			if (usage != null)
				usage.secondTime = bean.getThreadCpuTime(tid);
		}

		Collections.sort(usages);

		context.println("Thread CPU Usages");
		context.println("--------------------");
		for (ThreadCpuUsage usage : usages) {
			long elapsed = usage.secondTime - usage.firstTime;
			// remove just created thread or sleeping threads (noisy)
			if (elapsed <= 0)
				continue;

			context.printf("TID %d: %d\n", usage.tid, elapsed);
			StackTraceElement[] stack = findStack(stacks, usage.tid);
			for (StackTraceElement el : stack) {
				context.printf("\t%s.%s %s\n", el.getClassName(), el.getMethodName(), getFileAndLineNumber(el));
			}
		}
	}

	private ThreadCpuUsage find(List<ThreadCpuUsage> usages, long tid) {
		for (ThreadCpuUsage usage : usages)
			if (usage.tid == tid)
				return usage;

		return null;
	}

	private StackTraceElement[] findStack(Map<Thread, StackTraceElement[]> stacks, long tid) {
		for (Thread t : stacks.keySet())
			if (t.getId() == tid)
				return stacks.get(t);

		return null;
	}

	private String getFileAndLineNumber(StackTraceElement el) {
		if (el.getFileName() != null && el.getLineNumber() > 0)
			return String.format("(%s:%d)", el.getFileName(), el.getLineNumber());
		else if (el.getFileName() != null && el.getLineNumber() <= 0)
			return String.format("(%s)", el.getFileName());
		else
			return "";
	}

	private static class ThreadCpuUsage implements Comparable<ThreadCpuUsage> {
		private long tid;
		private long firstTime;
		private long secondTime;

		public ThreadCpuUsage(long tid, long firstTime) {
			this.tid = tid;
			this.firstTime = firstTime;
		}

		@Override
		public int compareTo(ThreadCpuUsage o) {
			// descending order
			long self = secondTime - firstTime;
			long other = o.secondTime - o.firstTime;
			return (int) (other - self);
		}
	}
}
