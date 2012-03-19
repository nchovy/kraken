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
 */package org.krakenapps.linux.api;

import java.io.IOException;

// ethtool
public class EthernetToolInformation {
	public static enum Duplex {
		Half, Full
	};

	private Integer speed; // Mb/s
	private Duplex duplex;
	private Boolean autoNegotiation;
	private Boolean linkDetected;

	private EthernetToolInformation() {
	}

	public Integer getSpeed() {
		return speed;
	}

	public Duplex getDuplex() {
		return duplex;
	}

	public Boolean getAutoNegotiation() {
		return autoNegotiation;
	}

	public Boolean getLinkDetected() {
		return linkDetected;
	}

	public static EthernetToolInformation getEthtoolInformation(String devname) throws IOException {
		String info = Util.run("ethtool " + devname);

		if (info.contains("No data available"))
			throw new IOException("no such device");
		if (info.contains("not installed"))
			throw new IOException("ethtool is not installed");

		EthernetToolInformation ethToolInfo = new EthernetToolInformation();
		String[] lines = info.split("\n");
		for (String line : lines) {
			line = line.trim();
			if (line.contains(":")) {
				String key = line.split(":", 2)[0].trim();
				String value = line.split(":", 2)[1].trim();

				if (key.equals("Speed"))
					ethToolInfo.speed = Integer.parseInt(value.replace("Mb/s", ""));
				else if (key.equals("Duplex"))
					ethToolInfo.duplex = (value.equals("Full")) ? Duplex.Full : Duplex.Half;
				else if (key.equals("Auto-negotiation"))
					ethToolInfo.autoNegotiation = (value.equals("on")) ? true : false;
				else if (key.equals("Link detected"))
					ethToolInfo.linkDetected = (value.equals("yes")) ? true : false;
			}
		}

		return ethToolInfo;
	}

	@Override
	public String toString() {
		return "EthernetToolInformation [speed=" + speed + ", duplex=" + duplex + ", autoNegotiation="
				+ autoNegotiation + ", linkDetected=" + linkDetected + "]";
	}

}
