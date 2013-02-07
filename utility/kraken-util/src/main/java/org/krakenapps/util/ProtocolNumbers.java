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
package org.krakenapps.util;

import java.util.HashMap;
import java.util.Map;

public class ProtocolNumbers {
	private static Map<String, Integer> numberMap;
	private static Map<Integer, String> nameMap;

	static {
		numberMap = new HashMap<String, Integer>();
		nameMap = new HashMap<Integer, String>();

		put(0, "HOPOPT");
		put(1, "ICMP");
		put(2, "IGMP");
		put(3, "GGP");
		put(4, "IP");
		put(5, "ST");
		put(6, "TCP");
		put(7, "CBT");
		put(8, "EGP");
		put(9, "IGP");
		put(10, "BBN-RCC-MON");
		put(11, "NVP-II");
		put(12, "PUP");
		put(13, "ARGUS");
		put(14, "EMCON");
		put(15, "XNET");
		put(16, "CHAOS");
		put(17, "UDP");
		put(18, "MUX");
		put(19, "DCN-MEAS");
		put(20, "HMP");
		put(21, "PRM");
		put(22, "XNS-IDP");
		put(23, "TRUNK-1");
		put(24, "TRUNK-2");
		put(25, "LEAF-1");
		put(26, "LEAF-2");
		put(27, "RDP");
		put(28, "IRTP");
		put(29, "ISO-TP4");
		put(30, "NETBLT");
		put(31, "MFE-NSP");
		put(32, "MERIT-INP");
		put(33, "DCCP");
		put(34, "3PC");
		put(35, "IDPR");
		put(36, "XTP");
		put(37, "DDP");
		put(38, "IDPR-CMTP");
		put(39, "TP++");
		put(40, "IL");
		put(41, "IPv6");
		put(42, "SDRP");
		put(43, "IPv6-Route");
		put(44, "IPv6-Frag");
		put(45, "IDRP");
		put(46, "RSVP");
		put(47, "GRE");
		put(48, "DSR");
		put(49, "BNA");
		put(50, "ESP");
		put(51, "AH");
		put(52, "I-NLSP");
		put(53, "SWIPE");
		put(54, "NARP");
		put(55, "MOBILE");
		put(56, "TLSP");
		put(57, "SKIP");
		put(58, "IPv6-ICMP");
		put(59, "IPv6-NoNxt");
		put(60, "IPv6-Opts");
		put(62, "CFTP");
		put(64, "SAT-EXPAK");
		put(65, "KRYPTOLAN");
		put(66, "RVD");
		put(67, "IPPC");
		put(69, "SAT-MON");
		put(70, "VISA");
		put(71, "IPCV");
		put(72, "CPNX");
		put(73, "CPHB");
		put(74, "WSN");
		put(75, "PVP");
		put(76, "BR-SAT-MON");
		put(77, "SUN-ND");
		put(78, "WB-MON");
		put(79, "WB-EXPAK");
		put(80, "ISO-IP");
		put(81, "VMTP");
		put(82, "SECURE-VMTP");
		put(83, "VINES");
		put(84, "TTP");
		put(85, "NSFNET-IGP");
		put(86, "DGP");
		put(87, "TCF");
		put(88, "EIGRP");
		put(89, "OSPFIGP");
		put(90, "Sprite-RPC");
		put(91, "LARP");
		put(92, "MTP");
		put(93, "AX.25");
		put(94, "IPIP");
		put(95, "MICP");
		put(96, "SCC-SP");
		put(97, "ETHERIP");
		put(98, "ENCAP");
		put(100, "GMTP");
		put(101, "IFMP");
		put(102, "PNNI");
		put(103, "PIM");
		put(104, "ARIS");
		put(105, "SCPS");
		put(106, "QNX");
		put(107, "A/N");
		put(108, "IPComp");
		put(109, "SNP");
		put(110, "Compaq-Peer");
		put(111, "IPX-in-IP");
		put(112, "VRRP");
		put(113, "PGM");
		put(115, "L2TP");
		put(116, "DDX");
		put(117, "IATP");
		put(118, "STP");
		put(119, "SRP");
		put(120, "UTI");
		put(121, "SMP");
		put(122, "SM");
		put(123, "PTP");
		put(124, "ISISoverIPv4");
		put(125, "FIRE");
		put(126, "CRTP");
		put(127, "CRUDP");
		put(128, "SSCOPMCE");
		put(129, "IPLT");
		put(130, "SPS");
		put(131, "PIPE");
		put(132, "SCTP");
		put(133, "FC");
		put(134, "RSVP-E2E-IGNORE");
		put(135, "MobilityHeader");
		put(136, "UDPLite");
		put(137, "MPLS-in-IP");
		put(138, "manet");
		put(139, "HIP");
		put(140, "Shim6");
	}

	private ProtocolNumbers() {
	}

	public static int getNumber(String protocolName) {
		return numberMap.get(protocolName.toUpperCase());
	}

	public static String getName(int number) {
		return nameMap.get(number);
	}

	private static void put(int number, String name) {
		numberMap.put(name.toUpperCase(), number);
		nameMap.put(number, name.toUpperCase());
	}
}
