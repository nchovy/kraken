package org.krakenapps.pcap.decoder.smb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.krakenapps.pcap.decoder.netbios.NetBiosDatagramPacket;
import org.krakenapps.pcap.decoder.netbios.NetBiosDatagramProcessor;
import org.krakenapps.pcap.decoder.netbios.NetBiosSessionPacket;
import org.krakenapps.pcap.decoder.netbios.NetBiosSessionProcessor;
import org.krakenapps.pcap.decoder.netbios.rr.DirectBroadcastData;
import org.krakenapps.pcap.decoder.smb.comparser.*;
import org.krakenapps.pcap.decoder.smb.request.NegotiateRequest;
import org.krakenapps.pcap.decoder.smb.request.TransactionRequest;
import org.krakenapps.pcap.decoder.smb.response.NegotiateResponse;
import org.krakenapps.pcap.decoder.smb.response.NegotiateSecurityExtendResponse;
import org.krakenapps.pcap.decoder.smb.response.TransactionResponse;
import org.krakenapps.pcap.decoder.smb.rr.SmbCommand;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.decoder.smb.udp.UdpTransaction;
import org.krakenapps.pcap.decoder.tcp.TcpSessionKey;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
import org.krakenapps.pcap.util.ChainBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmbDecoder implements NetBiosSessionProcessor, NetBiosDatagramProcessor {
	private final Logger logger = LoggerFactory.getLogger(SmbDecoder.class.getName());
	private Set<SmbProcessor> rpcCallbacks = new HashSet<SmbProcessor>();
	private Map<TcpSessionKey, SmbSession> sessions;
	private ComCommandMapper parsers;
	private UdpCommandMapper udpParsers;

	public SmbDecoder() {
		sessions = new HashMap<TcpSessionKey, SmbSession>();
		parsers = new ComCommandMapper();
		udpParsers = new UdpCommandMapper();
	}

	public void registerRpcCallbacks(SmbProcessor callback) {
		rpcCallbacks.add(callback);
	}

	public void unregisterRpcCallbacks(SmbProcessor callback) {
		rpcCallbacks.remove(callback);
	}

	@Override
	public void process(NetBiosDatagramPacket p) {
		Buffer b = new ChainBuffer();
		SmbPacket smbP = new SmbPacket();
		SmbSession session = new SmbSession();// null pointer
		((DirectBroadcastData) p.getData()).getUserData().discardReadBytes();
		b.addLast(((DirectBroadcastData) p.getData()).getUserData());
		smbP.header = SmbHeader.parse(b);
	//	System.out.println(smbP.header);
		SmbDataParser parser = udpParsers.getParser(smbP.header.getCommand());
		if (parser == null) {
			System.out.println("this packet have no more data");
			return;
		}
		smbP.data = parser.parseRequest(smbP.header, b, session);
		// Datagram data is Encoded Second level
		// composed of three part
		// SourceName(Max 255byte) , DestinationName(Max 255byte) , user's
		// Data(Max 512)
		// max 1064 , min 576byte
		if (smbP.header.getCommand() == SmbCommand.SMB_COM_TRANSACTION) {
			Buffer transSetup = new ChainBuffer();
			short op;
			transSetup.addLast(((UdpTransaction)smbP.data).getSetup());
			Buffer tmp = new ChainBuffer();
			tmp.addLast(((UdpTransaction) smbP.data).getTransData());
			op = ByteOrderConverter.swap(transSetup.getShort());
			if(op == 0x0001){
				for (SmbProcessor call : rpcCallbacks) {
					call.processMailslot(tmp);
				}
			}
			else{
				for (SmbProcessor call : rpcCallbacks) {
					call.processUdp(tmp);
				}
			}
		}
	}

	@Override
	public void processRx(NetBiosSessionPacket p, TcpSessionKey netBiosKey) {
		Buffer b = new ChainBuffer();
		SmbPacket smbP = new SmbPacket();
		(p.getData()).getBuffer().discardReadBytes();
		b.addLast((p.getData()).getBuffer());
		if (b.readableBytes() == 0) {
			return;
		}
		smbP.header = SmbHeader.parse(b);
		// System.out.println("RX Header : " + smbP.header);
		SmbSession session = sessions.get(netBiosKey);
		if (session == null) {
			logger.error("smb decoder: session not found [{}]", netBiosKey);
			return;
		}
		SmbDataParser parser = parsers.getComParser(smbP.header.getCommand());
		if (parser == null) {
			logger.error("smb RX decoder: command parser not found [{}]", smbP.header.getCommand());
			return;
		}
		session.getSession(smbP.header);
		smbP.data = parser.parseResponse(smbP.header, b, session);
		// System.out.println("RX Data: " + smbP.data);
		// only use negotiate packet
		if (smbP.header.getCommand() == SmbCommand.SMB_COM_NEGOTIATE) {
			int i;
			if (session.getNegotiateRequestHeader().isFlag2ExtendedSecurity()) {
				i = ((NegotiateSecurityExtendResponse) (smbP.data)).getDialectIndex();
			} else {
				i = ((NegotiateResponse) (smbP.data)).getDialectIndex();
			}
			session.setDialectIndex(i);
			session.setNegotiateResponseHeader(smbP.header);
			if (session.getNegotiateRequestHeader().isFlag2ExtendedSecurity()) {
				session.setExt(true);
				session.setNegotiateExtResponseData((NegotiateSecurityExtendResponse) smbP.data);
			} else {
				session.setNegotiateResponseData((NegotiateResponse) (smbP.data));
			}
		} else if (smbP.header.getCommand() == SmbCommand.SMB_COM_TRANSACTION) {
			Buffer tmp = new ChainBuffer();
			tmp.addLast(((TransactionResponse) smbP.data).getTransData());
			if (tmp.readableBytes() != 0) {
				for (SmbProcessor call : rpcCallbacks) {
					call.processTcpRx(tmp);
				}
			}
		}
	}

	@Override
	public void processTx(NetBiosSessionPacket p, TcpSessionKey netBiosKey) {
		Buffer b = new ChainBuffer();
		SmbPacket smbP = new SmbPacket();
		SmbSession session;
		(p.getData()).getBuffer().discardReadBytes();
		b.addLast((p.getData()).getBuffer());
		if (b.readableBytes() == 0) {
			return;
		}
		smbP.header = SmbHeader.parse(b);
		// System.out.println("TX Header : " + smbP.header);
		if ((session = sessions.get(netBiosKey)) == null) {
			session = new SmbSession(netBiosKey);
		}
		session.setSessionHeader(smbP.header);
		SmbDataParser parser = parsers.getComParser(smbP.header.getCommand());
		if (parser == null) {
			logger.error("smb TX decoder: command parser not found [{}]", smbP.header.getCommand());
			return;
		}
		smbP.data = parser.parseRequest(smbP.header, b, session);
		// requested Dialect array store
		if (smbP.header.getCommand() == SmbCommand.SMB_COM_NEGOTIATE) {
			session.setNegotiateRequestHeader(smbP.header);
			session.setNegotiateRequestData((NegotiateRequest) smbP.data);
		} else if (smbP.header.getCommand() == SmbCommand.SMB_COM_TRANSACTION) {
			Buffer tmp = new ChainBuffer();
			tmp.addLast(((TransactionRequest) smbP.data).getTransData());
			if (tmp.readableBytes() != 0) { // dataCount 0 can't decoding
				for (SmbProcessor call : rpcCallbacks) {
					call.processTcpTx(tmp);
				}
			}
		}
		// System.out.println("TX Data : " + smbP.data);
		session.setSessionData(smbP.header, smbP.data);
		sessions.put(netBiosKey, session);
	}
}
