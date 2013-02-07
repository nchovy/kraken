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

public class TcpHost {
	private InetAddress ipAddr;
	private int port;

	private int lastFrameReceived;
	private int lastAcceptableFrame;
	
	public TcpHost(TcpPacket packet) {
		ipAddr = packet.getSourceAddress();
		port = packet.getSourcePort();
		
		lastFrameReceived = 0;
		lastAcceptableFrame = 0;
	}

	public InetAddress getIpAddr() {
		return ipAddr;
	}

	public int getPort() {
		return port;
	}

	public int getLastFrameReceived() {
		return lastFrameReceived;
	}

	public void setLastFrameReceived(int received) {
		lastFrameReceived += received;
	}
	
	public int getLastAcceptableFrame() {
		return lastAcceptableFrame;
	}

	public void setLastAcceptableFrame(int windowSize) {
		lastAcceptableFrame = lastFrameReceived + windowSize;
	}
}