package org.krakenapps.dns;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DnsResolver {
	private final Logger logger = LoggerFactory.getLogger(DnsResolver.class.getName());

	private enum QueryType {
		A, NS, CNAME, SOA, PTR, HINFO, MX, AAAA
	}

	private static final int DNS_PORT = 53;

	public DnsReply dig(String[] args) {
		try {
			String domainName = "www.google.com";
			short queryType = 1;

			String[] tokens = args;

			if (tokens.length == 2) {
				queryType = queryTypeParser(getQueryType(tokens[0]));
				domainName = tokens[1];
			} else {
				domainName = tokens[0];
			}

			// create socket, packet, and packet encoding.
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(5000);
			byte[] buffer = generateDnsQueryPacket(domainName, queryType);

			// allocate array for reply packet.
			byte[] tempReplyBuffer = new byte[1000];
			ByteBuffer replyByteBuffer = ByteBuffer.allocate(1000);

			// sending packet in socket.
			InetAddress address = InetAddress.getByAddress(new byte[] { (byte) 168, (byte) 126, (byte) 63, (byte) 1 });

			DatagramPacket dnsQueryPacket = new DatagramPacket(buffer, buffer.length, address, DNS_PORT);
			socket.send(dnsQueryPacket);

			// packet received.
			DatagramPacket dnsReplyPacket = new DatagramPacket(tempReplyBuffer, tempReplyBuffer.length);
			socket.receive(dnsReplyPacket);
			replyByteBuffer.put(dnsReplyPacket.getData());
			replyByteBuffer.flip();

			// decoding received packet.
			DnsReply dnsReply = new DnsReply();
			DnsReplyDecoder decoder = new DnsReplyDecoder();
			decoder.decodeHeader(replyByteBuffer, dnsReply);

			// socket have to close.
			socket.close();

			return dnsReply;
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			logger.error("Time out");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private QueryType getQueryType(String queryType) {
		if (queryType.equals("NS") || queryType.equals("ns"))
			return QueryType.NS;
		else if (queryType.equals("CNAME") || queryType.equals("cname"))
			return QueryType.CNAME;
		else if (queryType.equals("SOA") || queryType.equals("soa"))
			return QueryType.SOA;
		else if (queryType.equals("PTR") || queryType.equals("ptr"))
			return QueryType.PTR;
		else if (queryType.equals("HINFO") || queryType.equals("hinfo"))
			return QueryType.HINFO;
		else if (queryType.equals("MX") || queryType.equals("mx"))
			return QueryType.MX;
		else if (queryType.equals("AAAA") || queryType.equals("aaaa"))
			return QueryType.AAAA;
		else
			return QueryType.A;
	}

	private short queryTypeParser(QueryType queryType) {
		switch (queryType) {
		case A:
			return 1;
		case NS:
			return 2;
		case CNAME:
			return 5;
		case SOA:
			return 6;
		case PTR:
			return 12;
		case HINFO:
			return 13;
		case MX:
			return 15;
		case AAAA:
			return 28;
		default:
			return 1;
		}
	}

	private byte[] generateDnsQueryPacket(String domainName, short queryType) {
		DnsFlags flags = new DnsFlags();
		flags.setRecursionDesired(true);

		DnsHeader header = new DnsHeader();
		header.setTransactionId((short) 2);

		header.setFlags(flags);
		header.setQuestions((short) 1);

		DnsQuery query = new DnsQuery();
		query.setDomainName(domainName);
		query.setQueryType(queryType);
		query.setQueryClass((short) 1);

		byte[] buffer = DnsQueryEncoder.encode(header, query);

		return buffer;
	}
}
