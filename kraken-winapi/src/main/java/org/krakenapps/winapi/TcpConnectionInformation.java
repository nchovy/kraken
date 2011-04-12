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

public class TcpConnectionInformation {
	public enum State {
		Unknown, Closed, Listen, SynSent, SynReceived, Established, FinWait1, FinWait2, CloseWait, Closing, LastACK, TimeWait, DeleteTCB
	}

	private InetSocketAddress local;
	private int localScopeId;
	private InetSocketAddress remote;
	private int remoteScopeId;
	private State state;
	private int pid;

	private TcpConnectionInformation(byte[] localAddress, int localScopeId, int localPort, byte[] remoteAddress,
			int remoteScopeId, int remotePort, String state, int pid) throws UnknownHostException {
		this.local = new InetSocketAddress(InetAddress.getByAddress(localAddress), localPort);
		this.localScopeId = localScopeId;
		this.remote = new InetSocketAddress(InetAddress.getByAddress(remoteAddress), remotePort);
		this.remoteScopeId = remoteScopeId;
		this.state = State.valueOf(state);
		this.pid = pid;
	}

	public InetSocketAddress getLocal() {
		return local;
	}

	public int getLocalScopeId() {
		return localScopeId;
	}

	public InetSocketAddress getRemote() {
		return remote;
	}

	public int getRemoteScopeId() {
		return remoteScopeId;
	}

	public State getState() {
		return state;
	}

	public int getPid() {
		return pid;
	}

	@Override
	public String toString() {
		return "local=" + local + ", remote=" + remote + ", state=" + state + ", pid=" + pid;
	}
}
