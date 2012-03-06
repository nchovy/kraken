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
package org.krakenapps.pcap.decoder.tcp;

import java.net.InetAddress;

/**
 * @author mindori
 */
public class TcpSessionKeyImpl implements TcpSessionKey {
	private InetAddress addr1;
	private InetAddress addr2;
	private int port1;
	private int port2;
	private boolean reversed = false;

	public TcpSessionKeyImpl(InetAddress clientIp, InetAddress serverIp, int clientPort, int serverPort) {
		if (clientIp.hashCode() < serverIp.hashCode()) {
			this.addr1 = clientIp;
			this.addr2 = serverIp;
			this.port1 = clientPort;
			this.port2 = serverPort;
		} else {
			this.addr2 = clientIp;
			this.addr1 = serverIp;
			this.port2 = clientPort;
			this.port1 = serverPort;
			reversed = true;
		}
	}

	public TcpSessionKeyImpl(TcpSessionKeyImpl other) {
		addr1 = other.getClientIp();
		addr2 = other.getServerIp();
		port1 = other.getClientPort();
		port2 = other.getServerPort();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		TcpSessionKeyImpl o = (TcpSessionKeyImpl) obj;
		if (!addr1.equals(o.addr1))
			return false;
		if (port1 != o.port1)
			return false;
		if (!addr2.equals(o.addr2))
			return false;
		if (port2 != o.port2)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return addr1.hashCode() ^ port1 ^ addr2.hashCode() ^ port2;
	}

	@Override
	public InetAddress getClientIp() {
		return reversed ? addr2 : addr1;
	}

	@Override
	public InetAddress getServerIp() {
		return reversed ? addr1 : addr2;
	}

	@Override
	public int getClientPort() {
		return reversed ? port2 : port1;
	}

	@Override
	public int getServerPort() {
		return reversed ? port1 : port2;
	}

	public void flip() {
		reversed = !reversed;
	}

	@Override
	public String toString() {
		return String.format("%s:%d -> %s:%d", getClientIp().getHostAddress(), getClientPort(), getServerIp()
				.getHostAddress(), getServerPort());
	}
}
