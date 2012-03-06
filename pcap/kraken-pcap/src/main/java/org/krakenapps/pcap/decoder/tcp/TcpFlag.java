package org.krakenapps.pcap.decoder.tcp;

public class TcpFlag {
	public static final int FIN = 0x1;
	public static final int SYN = 0x2;
	public static final int RST = 0x4;
	public static final int PSH = 0x8;
	public static final int ACK = 0x10;
	public static final int URG = 0x20;
	
	private TcpFlag() {
	}
}