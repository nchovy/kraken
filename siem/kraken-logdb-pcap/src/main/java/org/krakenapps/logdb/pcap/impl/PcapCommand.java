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
 */
package org.krakenapps.logdb.pcap.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.decoder.ethernet.EthernetProcessor;
import org.krakenapps.pcap.decoder.http.HttpDecoder;
import org.krakenapps.pcap.decoder.http.HttpProcessor;
import org.krakenapps.pcap.decoder.http.HttpRequest;
import org.krakenapps.pcap.decoder.http.HttpResponse;
import org.krakenapps.pcap.decoder.tcp.TcpDirection;
import org.krakenapps.pcap.decoder.tcp.TcpSegment;
import org.krakenapps.pcap.decoder.tcp.TcpSegmentCallback;
import org.krakenapps.pcap.decoder.tcp.TcpSession;
import org.krakenapps.pcap.decoder.udp.UdpPacket;
import org.krakenapps.pcap.decoder.udp.UdpProcessor;
import org.krakenapps.pcap.packet.PacketHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.PcapFileRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PcapCommand extends LogQueryCommand {
	private final Logger logger = LoggerFactory.getLogger(PcapCommand.class.getName());
	private PcapFileRunner runner;

	public PcapCommand(File f) {
		headerColumn.put("date", "_time");
		runner = new PcapFileRunner(f);
		runner.getEthernetDecoder().register(new EthernetProcessor() {

			@Override
			public void process(EthernetFrame frame) {
				LogMap m = new LogMap();
				PacketHeader h = frame.getPcapPacket().getPacketHeader();
				Date date = new Date(h.getTsSec() * 1000L + h.getTsUsec());
				m.put("_time", date);
				m.put("proto", "eth");
				m.put("ether_type", frame.getType());
				m.put("frame_size", frame.getData().readableBytes());
				m.put("dst", frame.getDestination().toString());
				m.put("src", frame.getSource().toString());
				write(m);
			}
		});

		runner.getTcpDecoder().registerSegmentCallback(new TcpSegmentCallback() {
			@Override
			public void onReceive(TcpSession session, TcpSegment segment) {
				if (logger.isDebugEnabled())
					logger.debug("kraken logdb pcap: tcp [{}]", session.getKey());

				Date date = getDate(segment.getIpPacket().getL2Frame());

				LogMap m = new LogMap();
				m.put("_time", date);
				m.put("proto", "tcp");
				m.put("src_ip", segment.getSourceAddress().getHostAddress());
				m.put("src_port", segment.getSourcePort());
				m.put("dst_ip", segment.getDestinationAddress().getHostAddress());
				m.put("dst_port", segment.getDestinationPort());
				m.put("client_ip", session != null ? session.getKey().getClientIp().getHostAddress() : null);
				m.put("client_port", session != null ? session.getKey().getClientPort() : null);
				m.put("server_ip", session != null ? session.getKey().getServerIp().getHostAddress() : null);
				m.put("server_port", session != null ? session.getKey().getServerPort() : null);
				m.put("client_state", session != null ? session.getClientState() : null);
				m.put("server_state", session != null ? session.getServerState() : null);
				if (segment.getDirection() == TcpDirection.ToServer) {
					m.put("sent", segment.getData() != null ? segment.getData().readableBytes() : 0);
					m.put("rcvd", 0);
				} else {
					m.put("sent", 0);
					m.put("rcvd", segment.getData() != null ? segment.getData().readableBytes() : 0);
				}

				m.put("seq", segment.getSeq());
				m.put("ack", segment.getAck());
				write(m);
			}
		});

		runner.getUdpDecoder().registerUdpProcessor(new UdpProcessor() {
			@Override
			public void process(UdpPacket p) {
				Date date = getDate(p.getIpPacket().getL2Frame());

				LogMap m = new LogMap();
				m.put("_time", date);
				m.put("proto", "udp");
				m.put("src_ip", p.getSource().getAddress().getHostAddress());
				m.put("src_port", p.getSourcePort());
				m.put("dst_ip", p.getDestination().getAddress().getHostAddress());
				m.put("dst_port", p.getDestinationPort());
				m.put("length", p.getLength());
				m.put("checksum", p.getChecksum());
				write(m);
			}
		});

		HttpDecoder http = new HttpDecoder();
		http.register(new HttpProcessor() {
			@Override
			public void onRequest(HttpRequest req) {
				LogMap m = new LogMap();
				m.put("proto", "http");
				m.put("type", "request");
				m.put("server_ip", req.getLocalAddress().getAddress().getHostAddress());
				m.put("client_ip", req.getRemoteAddress().getAddress().getHostAddress());
				m.put("method", req.getMethod());
				m.put("url", req.getURL().toString());
				m.put("version", req.getHttpVersion().toString());
				write(m);
			}

			@Override
			public void onResponse(HttpRequest req, HttpResponse resp) {
				LogMap m = new LogMap();
				m.put("proto", "http");
				m.put("type", "response");
				m.put("server_ip", req.getLocalAddress().getAddress().getHostAddress());
				m.put("client_ip", req.getRemoteAddress().getAddress().getHostAddress());
				m.put("method", req.getMethod());
				m.put("url", req.getURL().toString());
				m.put("status_code", resp.getStatusCode());
				m.put("status_line", resp.getStatusLine());
				write(m);
			}

			@Override
			public void onMultipartData(Buffer buffer) {
			}
		});

		runner.getTcpDecoder().getProtocolMapper().register(Protocol.HTTP, http);
	}

	private Date getDate(Object frame) {
		Date date = null;
		if (frame != null && frame instanceof EthernetFrame) {
			EthernetFrame eth = (EthernetFrame) frame;
			if (eth.getPcapPacket() != null)
				date = eth.getPcapPacket().getPacketHeader().getDate();
		}
		return date;
	}

	@Override
	public void start() {
		status = Status.Running;
		try {
			runner.run();
		} catch (IOException e) {
		} finally {
			eof();
		}
	}

	@Override
	public boolean isReducer() {
		return false;
	}

	@Override
	public void push(LogMap m) {
	}

}
