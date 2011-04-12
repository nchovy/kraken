package org.krakenapps.pcap.decoder.netbios;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.krakenapps.pcap.decoder.tcp.TcpProcessor;
import org.krakenapps.pcap.decoder.tcp.TcpSessionKey;
import org.krakenapps.pcap.decoder.udp.UdpPacket;
import org.krakenapps.pcap.decoder.udp.UdpProcessor;
import org.krakenapps.pcap.util.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetBiosDecoder implements TcpProcessor, UdpProcessor {
	private final Logger logger = LoggerFactory.getLogger(NetBiosDecoder.class.getName());

	private NetBiosDatagramService datagramService;
	private NetBiosNameService nameService;
	private NetBiosSessionService sessionService;

	private Set<NetBiosNameProcessor> nameCallbacks = new CopyOnWriteArraySet<NetBiosNameProcessor>();
	private Set<NetBiosSessionProcessor> sessionCallbacks = new CopyOnWriteArraySet<NetBiosSessionProcessor>();
	private Set<NetBiosDatagramProcessor> datagramCallbacks = new CopyOnWriteArraySet<NetBiosDatagramProcessor>();

	public NetBiosDecoder() {
		datagramService = new NetBiosDatagramService();
		nameService = new NetBiosNameService();
		sessionService = new NetBiosSessionService();
	}

	public void registerNameProcessor(NetBiosNameProcessor callback) {
		nameCallbacks.add(callback);
	}

	public void unregisterNameProcessor(NetBiosNameProcessor callback) {
		nameCallbacks.remove(callback);
	}

	public void registerDatagramProcessor(NetBiosDatagramProcessor callback) {
		datagramCallbacks.add(callback);
	}

	public void unregisterDatagramProcessor(NetBiosDatagramProcessor callback) {
		datagramCallbacks.remove(callback);
	}

	public void registerSessionProcessor(NetBiosSessionProcessor callback) {
		sessionCallbacks.add(callback);
	}

	public void unregisterSessionProcessor(NetBiosSessionProcessor callback) {
		sessionCallbacks.remove(callback);
	}

	@Override
	public void handleRx(TcpSessionKey key, Buffer b) {
		NetBiosSessionPacket packet = sessionService.parseRx(b);

		if (NetBiosSessionType.isSessionMessage(packet)) {
			for (NetBiosSessionProcessor callback : sessionCallbacks) {
				callback.processRx(packet, key);
			}
		}
	}

	@Override
	public void handleTx(TcpSessionKey key, Buffer b) {
		NetBiosSessionPacket packet = sessionService.parseTx(b);

		if (NetBiosSessionType.isSessionMessage(packet)) {
			for (NetBiosSessionProcessor callback : sessionCallbacks) {
				callback.processTx(packet, key);
			}
		}
	}

	@Override
	public void onEstablish(TcpSessionKey key) {
	}

	@Override
	public void onFinish(TcpSessionKey key) {
	}

	@Override
	public void onReset(TcpSessionKey key) {
	}

	@Override
	public void process(UdpPacket p) {
		int port = p.getDestinationPort();

		Buffer b = p.getData();
		if (port == 137) {
			NetBiosNamePacket namePacket = nameService.parse(b);
			namePacket.setUdpPacket(p);

			for (NetBiosNameProcessor callback : nameCallbacks) {
				try {
					callback.process(namePacket);
				} catch (Exception e) {
					logger.warn("kraken netbios decoder: name processor should not throw any exception", e);
				}
			}
		} else if (port == 138) {
			NetBiosDatagramPacket datagramPacket = datagramService.parse(b);
			datagramPacket.setUdpPacket(p);
			switch (datagramPacket.getHeader().getMsgType()) {
			case DirectUniqueDatagram:
			case DirectGroupDatagram:
			case BroadcastDatagram: {
				for (NetBiosDatagramProcessor callback : datagramCallbacks) {
					callback.process(datagramPacket);
				}
			}
				break;
			default:
				return;
			}
		}
	}
}
