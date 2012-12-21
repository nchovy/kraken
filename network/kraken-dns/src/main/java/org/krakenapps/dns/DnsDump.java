package org.krakenapps.dns;

import java.net.DatagramPacket;

public class DnsDump {
	
	public static String dumpPacket(DatagramPacket packet) {
		StringBuilder sb = new StringBuilder();
		byte[] buf = packet.getData();
		int offset = packet.getOffset();
		int length = packet.getLength();

		sb.append("byte[] b = new byte[] { ");
		for (int i = 0; i < length; i++) {
			if (i != 0)
				sb.append(", ");
			sb.append(String.format("(byte) 0x%02x", buf[i + offset]));
		}
		sb.append("};");

		return sb.toString();
	}

}
