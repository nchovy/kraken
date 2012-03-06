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
package org.krakenapps.pcap.decoder.icmp;

/**
 * ICMP message
 * 
 * @author xeraph
 */
public class IcmpMessage {
	private IcmpMessage() {
	}

	public static String getMessage(int type, int code) {
		if (type == 8 && code == 0)
			return "Echo request";

		if (type == 0 && code == 0)
			return "Echo reply";

		if (type == 9 && code == 0)
			return "Router Advertisement";

		if (type == 10 && code == 0)
			return "Router discovery/selection/solicitation";

		if (type == 11 && code == 0)
			return "TTL expired in transit";

		if (type == 11 && code == 0)
			return "Fragment reassembly time exceeded";

		if (type == 3) {
			switch (code) {
			case 0:
				return "Destination network unreachable";
			case 1:
				return "Destination host unreachable";
			case 2:
				return "Destination protocol unreachable";
			case 3:
				return "Destination port unreachable";
			case 4:
				return "Fragmentation required";
			case 5:
				return "Source route failed";
			case 6:
				return "Destination network unknown";
			case 7:
				return "Destination host unknown";
			case 8:
				return "Source host isolated";
			case 9:
				return "Network administratively prohibited";
			case 10:
				return "Host administratively prohibited";
			case 11:
				return "Network unreachable for TOS";
			case 12:
				return "Host unreachable for TOS";
			case 13:
				return "Communication administratively prohibited";
			}
		}

		if (type == 4 && code == 0)
			return "Source quench";

		if (type == 5) {
			switch (code) {
			case 0:
				return "Redirect Datagram for the Network";
			case 1:
				return "Redirect Datagram for the Host";
			case 2:
				return "Redirect Datagram for the TOS & network";
			case 3:
				return "Redirect Datagram for the TOS & host";
			}
		}

		if (type == 6)
			return "Alternate Host Address";

		if (type == 12) {
			switch (code) {
			case 0:
				return "Pointer indicates the error";
			case 1:
				return "Missing a required option";
			case 2:
				return "Bad length";
			}
		}

		if (type == 13 && code == 0)
			return "Timestamp";

		if (type == 14 && code == 0)
			return "Timestamp reply";

		if (type == 15 && code == 0)
			return "Information Request";

		if (type == 16 && code == 0)
			return "Information Reply";

		if (type == 17 && code == 0)
			return "Address Mask Request";

		if (type == 18 && code == 0)
			return "Address Mask Reply";

		if (type == 30 && code == 0)
			return "Information Request";

		if (type == 31)
			return "Datagram Conversion Error";

		if (type == 32)
			return "Mobile Host Redirect";

		if (type == 33)
			return "Were-Are-You";

		if (type == 34)
			return "Here-I-Am";

		if (type == 35)
			return "Mobile Registration Request";

		if (type == 36)
			return "Mobile Registration Reply";

		if (type == 37)
			return "Domain Name Request";

		if (type == 38)
			return "Domain Name Reply";

		if (type == 39)
			return "SKIP Alogirithm Discovery Protocol";

		if (type == 40)
			return "Photuris, Security failures";

		if (type == 41)
			return "ICMP for experimental mobility protocols";

		return "Reserved";
	}

}
