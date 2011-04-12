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
package org.krakenapps.pcap.decoder.icmpv6;

/**
 * @author xeraph
 */
public class Icmpv6Message {
	private Icmpv6Message() {
	}

	public static String getMessage(int type) {
		switch (type & 0xFF) {
		case 1:
			return "Destination Unreachable";
		case 2:
			return "Packet Too Big";
		case 3:
			return "Time Exceeded";
		case 4:
			return "Parameter Problem";
		case 127:
			return "ICMPv6 error message";
		case 128:
			return "Echo Request";
		case 129:
			return "Echo Reply";
		case 133:
			return "Router Solicitation";
		case 134:
			return "Router Advertisement";
		case 135:
			return "Neighbor Solicitation";
		case 136:
			return "Neighbor Advertisement";
		default:
			return "reserved";
		}
	}
}
