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
package org.krakenapps.pcap.decoder.dhcp.options;

public enum DhcpOptionCode {
	Pad(0),
	SubnetMask(1),
	TimeOffset(2),
	Router(3),
	TimeServer(4),
	NameServer(5),
	DomainNameServer(6),
	LogServer(7),
	QuoteServer(8),
	LprServer(9),
	ImpressServer(10),
	ResourceLocationServer(11),
	HostName(12),
	BootFileSize(13),
	MeritDumpFile(14),
	DomainName(15),
	SwapServer(16),
	RootPath(17),
	ExtensionsPath(18),
	IpForwarding(19),
	NonLocalSourceRouting(20),
	PolicyFilter(21),
	MaxDatagramReassemblySize(22),
	DefaultIpTTL(23),
	PathMtuAgingTimeout(24),
	PathMtuPlateauTable(25),
	InterfaceMtu(26),
	AllSubnetsAreLocal(27),
	BroadcastAddress(28),
	PerformMaskDiscovery(29),
	MaskSupplier(30),
	PerformRouterDiscovery(31),
	RouterSolicitationAddress(32),
	StaticRoutingTable(33),
	TrailerEncapsulation(34),
	ArpCacheTimeout(35),
	EthernetEncapsulation(36),
	DefaultTcpTTL(37),
	TcpKeepaliveInterval(38),
	TcpKeepaliveGarbage(39),
	NetworkInformationServiceDomain(40),
	NetworkInformationServers(41),
	NtpServers(42),
	VendorSpecificInfo(43),
	NetbiosNameServer(44),
	NetbiosDatagramDistributionServer(45),
	NetbiosNodeType(46),
	NetbiosScope(47),
	XWindowSystemFontServer(48),
	XWindowSystemDisplayManager(49),
	RequestedIpAddress(50),
	IpAddressLeaseTime(51),
	OptionOverload(52),
	DhcpMessageType(53),
	ServerIdentifier(54),
	ParameterRequestList(55),
	Message(56),
	MaxDhcpMessageSize(57),
	RenewTimeValue(58),
	RebindingTimeValue(59),
	ClientIdentifier(60),
	ClientIdentifier2(61),
	NetwareDomainName(62),
	NetwareIp(63),
	NetworkInformationServicePlusDomain(64),
	NetworkInformationServicePlusServers(65),
	TftpServerName(66),
	BootfileName(67),
	MobileIpHomeAgent(68),
	SmtpServer(69),
	PopServer(70),
	NntpServer(71),
	DefaultWebServer(72),
	DefaultFingerServer(73),
	DefaultIrcServer(74),
	StreetTalkServer(75),
	StreetTalkDirectoryAssistanceServer(76),
	UserClassInfo(77),
	SlpDirectoryAgent(78),
	SlpServiceScope(79),
	FQDN(81),
	RelayAgentInfo(82),
	InternetStorageNameService(83),
	NdsServers(85),
	NdsTreeName(86),
	NdsContext(87),
	BcmcsControllerDomainNameList(88),
	BcmcsControllerIpv6AddressList(89),
	Authentication(90),
	LDAP(95);
	
	private int code;
	
	private DhcpOptionCode(int code) {
		this.code = code;
	}
	
	public int value() {
		return code;
	}
	
	public static DhcpOptionCode find(int code) {
		for (DhcpOptionCode c : DhcpOptionCode.values())
			if (c.value() == code)
				return c;
		
		return null;
	}
}
