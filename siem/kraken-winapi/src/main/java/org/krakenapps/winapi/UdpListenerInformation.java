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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class UdpListenerInformation {
	private InetSocketAddress local;
	private int localScopeId;
	private int pid;

	public UdpListenerInformation(byte[] localAddress, int localScopeId, int localPort, int pid) throws UnknownHostException {
		this.local = new InetSocketAddress(InetAddress.getByAddress(localAddress), localPort);
		this.localScopeId = localScopeId;
		this.pid = pid;
	}

	public InetSocketAddress getLocal() {
		return local;
	}

	public int getLocalScopeId() {
		return localScopeId;
	}

	public int getPid() {
		return pid;
	}

	@Override
	public String toString() {
		return "local=" + local + ", pid=" + pid;
	}
}
