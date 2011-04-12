package org.krakenapps.sleepproxy.impl;

import java.net.InetAddress;
import java.util.Scanner;

public class LogParser {
	private LogParser() {
	}

	public static LogMessage parse(String rawLog) {
		try {
			LogMessage msg = new LogMessage();
			Scanner scanner = new Scanner(rawLog);
			scanner.useDelimiter(",");

			msg.setRawLog(rawLog);
			msg.setVersion(Integer.valueOf(scanner.next()));
			msg.setMsgType(Integer.valueOf(scanner.next()));
			msg.setGuid(scanner.next());
			msg.setUserName(scanner.next());
			msg.setHostName(scanner.next());
			msg.setDomain(scanner.next());

			int count = Integer.valueOf(scanner.next());

			for (int i = 0; i < count; i++) {
				NicInfo nic = new NicInfo();
				nic.setMac(scanner.next());
				nic.setIp(InetAddress.getByName(scanner.next()));
				nic.setDescription(scanner.next().replaceAll("\"", ""));
				msg.getNetworkAdapters().add(nic);
			}

			return msg;
		} catch (Exception e) {
			throw new IllegalArgumentException("cannot parse log: " + rawLog);
		}
	}
}
