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
package org.krakenapps.snmp;

import java.net.InetSocketAddress;

public class SnmpTrapBinding {
	private String name;
	private InetSocketAddress bindAddress;
	private int threadCount;

	public SnmpTrapBinding() {
		this.bindAddress = new InetSocketAddress(162);
		this.threadCount = 1;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public InetSocketAddress getListenAddress() {
		return bindAddress;
	}

	public void setBindAddress(InetSocketAddress bindAddress) {
		this.bindAddress = bindAddress;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	@Override
	public String toString() {
		return "name=" + name + ", listen=" + bindAddress + ", thread=" + threadCount;
	}

}
