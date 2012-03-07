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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;
import java.util.List;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;

public class PerfScript implements Script {
	private ScriptContext context;

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void processors(String[] args) {
		Runtime rt = Runtime.getRuntime();
		int processors = rt.availableProcessors();
		context.println("Processors: " + processors);
	}

	public void memory(String[] args) {
		Runtime rt = Runtime.getRuntime();
		long free = rt.freeMemory();
		long total = rt.totalMemory();
		long used = total - free;
		long max = rt.maxMemory();

		context.println("-------------------------");
		context.println("Used memory: " + formatNumber(used) + " bytes");
		context.println("Free memory: " + formatNumber(free) + " bytes");
		context.println("Total memory: " + formatNumber(total) + " bytes");
		context.println("Maximum memory: " + formatNumber(max) + " bytes");

		List<MemoryPoolMXBean> memoryPoolBeans = ManagementFactory.getMemoryPoolMXBeans();
		if (memoryPoolBeans != null) {
			for (MemoryPoolMXBean bean : memoryPoolBeans) {
				context.println("-------------------------");
				context.printf("%s (%s)\n", bean.getName(), bean.getType().toString());
				context.printf("  Committed: %s\n", formatNumber(bean.getUsage().getCommitted()));
				context.printf("  Init: %s\n", formatNumber(bean.getUsage().getInit()));
				context.printf("  Max: %s\n", formatNumber(bean.getUsage().getMax()));
				context.printf("  Used: %s\n", formatNumber(bean.getUsage().getUsed()));
			}
		}
	}

	private String formatNumber(long bytes) {
		DecimalFormat formatter = new DecimalFormat("###,###");
		return formatter.format(bytes);
	}

	public void gc(String[] args) {
		Runtime rt = Runtime.getRuntime();
		rt.gc();
		context.println("gc called");
		context.println("pending object finalization count: "
				+ ManagementFactory.getMemoryMXBean().getObjectPendingFinalizationCount());
	}

	public void system(String[] args) {
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		context.println("Architecture: " + os.getArch());
		context.println("Operating system: " + os.getName() + " " + os.getVersion());
		double systemLoadAverage = os.getSystemLoadAverage();
		if (systemLoadAverage < 0) {
			context.println("Load average: N/A");
		} else {
			context.println("Load average: " + systemLoadAverage);
		}
		context.printf("System Uptime: %d ms\n", ManagementFactory.getRuntimeMXBean().getUptime());
	}
}
