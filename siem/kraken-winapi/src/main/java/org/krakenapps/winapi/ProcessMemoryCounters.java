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

public class ProcessMemoryCounters {
	private long pageFaultCount;
	private long peakWorkingSetSize;
	private long workingSetSize;
	private long quotaPeakPagedPoolUsage;
	private long quotaPagedPoolUsage;
	private long quotaPeakNonPagedPoolUsage;
	private long quotaNonPagedPoolUsage;
	private long pagefileUsage;
	private long peakPagefileUsage;
	private long privateUsage;

	public ProcessMemoryCounters() {
	}

	public ProcessMemoryCounters(long pageFaultCount, long peakWorkingSetSize, long workingSetSize,
			long quotaPeakPagedPoolUsage, long quotaPagedPoolUsage, long quotaPeakNonPagedPoolUsage,
			long quotaNonPagedPoolUsage, long pagefileUsage, long peakPagefileUsage, long privateUsage) {
		this.pageFaultCount = pageFaultCount;
		this.peakWorkingSetSize = peakWorkingSetSize;
		this.workingSetSize = workingSetSize;
		this.quotaPeakPagedPoolUsage = quotaPeakPagedPoolUsage;
		this.quotaPagedPoolUsage = quotaPagedPoolUsage;
		this.quotaPeakNonPagedPoolUsage = quotaPeakNonPagedPoolUsage;
		this.quotaNonPagedPoolUsage = quotaNonPagedPoolUsage;
		this.pagefileUsage = pagefileUsage;
		this.peakPagefileUsage = peakPagefileUsage;
		this.privateUsage = privateUsage;
	}

	public long getPageFaultCount() {
		return pageFaultCount;
	}

	public long getPeakWorkingSetSize() {
		return peakWorkingSetSize;
	}

	public long getWorkingSetSize() {
		return workingSetSize;
	}

	public long getQuotaPeakPagedPoolUsage() {
		return quotaPeakPagedPoolUsage;
	}

	public long getQuotaPagedPoolUsage() {
		return quotaPagedPoolUsage;
	}

	public long getQuotaPeakNonPagedPoolUsage() {
		return quotaPeakNonPagedPoolUsage;
	}

	public long getQuotaNonPagedPoolUsage() {
		return quotaNonPagedPoolUsage;
	}

	public long getPagefileUsage() {
		return pagefileUsage;
	}

	public long getPeakPagefileUsage() {
		return peakPagefileUsage;
	}

	public long getPrivateUsage() {
		return privateUsage;
	}

}
