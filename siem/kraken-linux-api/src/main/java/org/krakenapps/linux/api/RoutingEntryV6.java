/*
 * Copyright 2011 Future Systems
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

import org.krakenapps.linux.api.RoutingEntry.Flag;

public class RoutingEntryV6 {
	private String destination;
	private String nextHop;
	private int mask;
	private Flag flags;
	private int metric;
	private int ref;
	private int use;
	private String iface;
	
	public RoutingEntryV6(String destination, String nextHop, int mask, Flag flags, int metric, int ref, int use, String iface) {
		this.destination = destination;
		this.nextHop = nextHop;
		this.mask = mask;
		this.flags = flags;
		this.metric = metric;
		this.ref = ref;
		this.use = use;
		this.iface = iface;
	}
	
	public String getDestination() {
		return destination;
	}

	public String getNextHop() {
		return nextHop;
	}

	public int getMask() {
		return mask;
	}
	
	public Flag getFlags() {
		return flags;
	}

	public int getMetric() {
		return metric;
	}

	public int getRef() {
		return ref;
	}

	public int getUse() {
		return use;
	}

	public String getIface() {
		return iface;
	}
}