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