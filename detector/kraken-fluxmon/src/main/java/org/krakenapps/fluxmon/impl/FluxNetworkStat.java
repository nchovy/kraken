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
package org.krakenapps.fluxmon.impl;

public class FluxNetworkStat implements Comparable<FluxNetworkStat> {
	private String domain;
	private int hostCount;

	public FluxNetworkStat(String domain, int count) {
		this.domain = domain;
		this.hostCount = count;
	}

	public String getDomain() {
		return domain;
	}

	public int getHostCount() {
		return hostCount;
	}

	@Override
	public int compareTo(FluxNetworkStat o) {
		return o.hostCount - hostCount; // descending order
	}

	@Override
	public String toString() {
		return domain + ": " + hostCount;
	}
	
}
