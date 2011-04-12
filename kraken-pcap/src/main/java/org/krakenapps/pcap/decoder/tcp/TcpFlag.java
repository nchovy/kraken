package org.krakenapps.pcap.decoder.tcp;

public class TcpFlag {
	public static int FIN = 0x1;
	public static int SYN = 0x2;
	public static int RST = 0x4;
	public static int PSH = 0x8;
	public static int ACK = 0x10;
	public static int URG = 0x20;
	
	private TcpFlag() {
	}
}
