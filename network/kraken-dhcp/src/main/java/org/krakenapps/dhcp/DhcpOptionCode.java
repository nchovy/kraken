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
package org.krakenapps.dhcp;

import java.net.InetAddress;

public enum DhcpOptionCode {
	Pad(0),
	SubnetMask(1, InetAddress.class),
	TimeOffset(2, Integer.class),
	Router(3, InetAddress[].class),
	TimeServer(4, InetAddress[].class),
	NameServer(5, InetAddress[].class),
	DomainNameServer(6, InetAddress[].class),
	LogServer(7, InetAddress[].class),
	QuoteServer(8, InetAddress[].class),
	LprServer(9, InetAddress[].class),
	ImpressServer(10, InetAddress[].class),
	ResourceLocationServer(11, InetAddress[].class),
	HostName(12, String.class),
	BootFileSize(13, Short.class),
	MeritDumpFile(14, String.class),
	DomainName(15, String.class),
	SwapServer(16, InetAddress.class),
	RootPath(17, String.class),
	ExtensionsPath(18, String.class),
	IpForwarding(19, Boolean.class),
	NonLocalSourceRouting(20, Boolean.class),
	PolicyFilter(21, InetAddress[].class), // (subnet & mask) pairs
	MaxDatagramReassemblySize(22, Short.class),
	DefaultIpTTL(23, Byte.class),
	PathMtuAgingTimeout(24, Integer.class),
	PathMtuPlateauTable(25, Short[].class),
	InterfaceMtu(26, Short.class),
	AllSubnetsAreLocal(27, Boolean.class),
	BroadcastAddress(28, InetAddress.class),
	PerformMaskDiscovery(29, Boolean.class),
	MaskSupplier(30, Boolean.class),
	PerformRouterDiscovery(31, Boolean.class),
	RouterSolicitationAddress(32, InetAddress.class),
	StaticRoutingTable(33, InetAddress[].class), // (destination & router) pairs
	TrailerEncapsulation(34, Boolean.class),
	ArpCacheTimeout(35, Integer.class),
	EthernetEncapsulation(36, Boolean.class),
	DefaultTcpTTL(37, Byte.class),
	TcpKeepaliveInterval(38, Integer.class),
	TcpKeepaliveGarbage(39, Boolean.class),
	NetworkInformationServiceDomain(40, String.class),
	NetworkInformationServers(41, InetAddress[].class),
	NtpServers(42, InetAddress[].class),
	VendorSpecificInfo(43, String.class),
	NetbiosNameServer(44, InetAddress[].class),
	NetbiosDatagramDistributionServer(45, InetAddress[].class),
	NetbiosNodeType(46, Byte.class),
	NetbiosScope(47, String.class),
	XWindowSystemFontServer(48, InetAddress[].class),
	XWindowSystemDisplayManager(49, InetAddress[].class),
	RequestedIpAddress(50, InetAddress.class),
	IpAddressLeaseTime(51, Integer.class),
	OptionOverload(52, Byte.class),
	DhcpMessageType(53, Byte.class),
	ServerIdentifier(54, InetAddress.class),
	ParameterRequestList(55, Byte[].class),
	Message(56, String.class),
	MaxDhcpMessageSize(57, Short.class),
	RenewTimeValue(58, Integer.class),
	RebindingTimeValue(59, Integer.class),
	VendorClassIdentifier(60, String.class),
	ClientIdentifier(61, Byte[].class),
	NetwareDomainName(62),
	NetwareIp(63),
	NetworkInformationServicePlusDomain(64, String.class),
	NetworkInformationServicePlusServers(65, InetAddress[].class),
	TftpServerName(66, String.class),
	BootfileName(67, String.class),
	MobileIpHomeAgent(68, InetAddress[].class),
	SmtpServer(69, InetAddress[].class),
	PopServer(70, InetAddress[].class),
	NntpServer(71, InetAddress[].class),
	DefaultWebServer(72, InetAddress[].class),
	DefaultFingerServer(73, InetAddress[].class),
	DefaultIrcServer(74, InetAddress[].class),
	StreetTalkServer(75, InetAddress[].class),
	StreetTalkDirectoryAssistanceServer(76, InetAddress[].class),
	UserClassInfo(77),
	SlpDirectoryAgent(78),
	SlpServiceScope(79),
	FQDN(81),
	RelayAgentInfo(82),
	InternetStorageNameService(83),
	NdsServers(85),
	NdsTreeName(86, InetAddress[].class),
	NdsContext(87, String.class),
	BcmcsControllerDomainNameList(88),
	BcmcsControllerIpv6AddressList(89),
	Authentication(90),
	LDAP(95);

	private int code;
	private Class<?> clazz;

	private DhcpOptionCode(int code) {
		this.code = code;
	}

	private DhcpOptionCode(int code, Class<?> clazz) {
		this.code = code;
		this.clazz = clazz;
	}

	public int code() {
		return code;
	}

	public Class<?> getValueType() {
		return clazz;
	}

	public static DhcpOptionCode from(int code) {
		for (DhcpOptionCode c : DhcpOptionCode.values())
			if (c.code() == code)
				return c;

		return null;
	}
}
