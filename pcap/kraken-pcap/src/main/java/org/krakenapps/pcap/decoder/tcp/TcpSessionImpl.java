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
package org.krakenapps.pcap.decoder.tcp;

import java.util.concurrent.atomic.AtomicInteger;

import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

public class TcpSessionImpl implements TcpSession {
	private static AtomicInteger LAST_ID = new AtomicInteger(1);
	private int id;

	private TcpSessionKey key;

	private TcpHost client;
	private TcpHost server;

	private int clientFirstSeq;
	private int serverFirstSeq;

	private TcpStreamOption clientStreamOption;
	private TcpStreamOption serverStreamOption;

	private int clientFlags;
	private int serverFlags;

	private TcpState clientState;
	private TcpState serverState;

	private boolean isRegisterProtocol;
	private Protocol protocol;

	private Buffer clientSent;
	private Buffer serverSent;

	private WaitQueue clientQueue;
	private WaitQueue serverQueue;

	private ApplicationLayerMapper l7Mapper;

	private int packetCountAfterFin = 0;
	private int firstFinSeq = -1;
	private int firstFinAck = -1;

	public TcpSessionImpl(TcpProtocolMapper mapper) {
		id = LAST_ID.getAndIncrement();
		key = null;

		clientStreamOption = TcpStreamOption.NORMAL;
		serverStreamOption = TcpStreamOption.NORMAL;

		clientState = TcpState.LISTEN;
		serverState = TcpState.LISTEN;

		clientSent = new ChainBuffer();
		serverSent = new ChainBuffer();

		clientQueue = new WaitQueue();
		serverQueue = new WaitQueue();

		l7Mapper = new ApplicationLayerMapper(mapper);
	}

	public int getId() {
		return id;
	}

	public TcpSessionKey getKey() {
		return key;
	}

	public void setKey(TcpSessionKey key) {
		this.key = key;
	}

	public TcpHost getClient() {
		return client;
	}

	public void createClient(TcpPacket packet) {
		client = new TcpHost(packet);
	}

	public TcpHost getServer() {
		return server;
	}

	public void createServer(TcpPacket packet) {
		server = new TcpHost(packet);
	}

	/* constraint: one-time called */
	public void setClientFirstSeq(int clientFirstSeq) {
		this.clientFirstSeq = clientFirstSeq;
	}

	public int retRelativeClientSeq(int sequenceNumber) {
		return (sequenceNumber - clientFirstSeq);
	}

	/* constraint: one-time called */
	public void setServerFirstSeq(int serverFirstSeq) {
		this.serverFirstSeq = serverFirstSeq;
	}

	public int retRelativeServerSeq(int sequenceNumber) {
		return (sequenceNumber - serverFirstSeq);
	}

	public TcpStreamOption getClientStreamOption() {
		return clientStreamOption;
	}

	public void setClientStreamOption(TcpStreamOption clientStreamOption) {
		this.clientStreamOption = clientStreamOption;
	}

	public TcpStreamOption getServerStreamOption() {
		return serverStreamOption;
	}

	public void setServerStreamOption(TcpStreamOption serverStreamOption) {
		this.serverStreamOption = serverStreamOption;
	}

	public int getClientFlags() {
		return clientFlags;
	}

	public void setClientFlags(int clientFlags) {
		this.clientFlags = clientFlags;
	}

	public int getServerFlags() {
		return serverFlags;
	}

	public void setServerFlags(int serverFlags) {
		this.serverFlags = serverFlags;
	}

	public TcpState getClientState() {
		return clientState;
	}

	public void setClientState(TcpState clientState) {
		this.clientState = clientState;
	}

	public TcpState getServerState() {
		return serverState;
	}

	public void setServerState(TcpState serverState) {
		this.serverState = serverState;
	}

	public boolean isRegisterProtocol() {
		return isRegisterProtocol;
	}

	public void setRegisterProtocol(boolean isRegisterProtocol) {
		this.isRegisterProtocol = isRegisterProtocol;
	}

	public void registerProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public void unregisterProtocol(Protocol protocol) {
		if (this.protocol == protocol)
			this.protocol = null;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void storeToClientSent(Buffer data) {
		clientSent.addLast(data);
	}

	public void storeToServerSent(Buffer data) {
		serverSent.addLast(data);
	}

	public void pushToClient(Buffer data) { 
		l7Mapper.sendToApplicationLayer(protocol, key, TcpDirection.ToClient, data);
	}
	
	public void pushToServer(Buffer data) { 
		l7Mapper.sendToApplicationLayer(protocol, key, TcpDirection.ToServer, data);
	}

	public void pushToClientSack(Buffer data) {
		l7Mapper.sendToApplicationLayer(protocol, key, TcpDirection.ToServer, data);
	}

	public void pushToServerSack(Buffer data) {
		l7Mapper.sendToApplicationLayer(protocol, key, TcpDirection.ToClient, data);
	}

	public WaitQueue getClientQueue() {
		return clientQueue;
	}

	public WaitQueue getServerQueue() {
		return serverQueue;
	}

	public int getPacketCountAfterFin() {
		return packetCountAfterFin;
	}

	public void setPacketCountAfterFin(int packetCountAfterFin) {
		this.packetCountAfterFin = packetCountAfterFin;
	}

	public int getFirstFinSeq() {
		return firstFinSeq;
	}

	public void setFirstFinSeq(int firstFinSeq) {
		this.firstFinSeq = firstFinSeq;
	}

	public int getFirstFinAck() {
		return firstFinAck;
	}

	public void setFirstFinAck(int firstFinAck) {
		this.firstFinAck = firstFinAck;
	}

	public void doEstablish(TcpSessionTable sessionTable, TcpSessionImpl session, TcpPacket packet, TcpStateUpdater stateUpdater) {
		sessionTable.doEstablish(session, packet, stateUpdater);
	}

	public void close(TcpSessionTable sessionTable, TcpSessionImpl session, TcpPacket packet) {
		sessionTable.close(packet);
	}

	public void setRelativeNumbers(TcpPacket packet) {
		switch (packet.getFlags()) {
		case TcpFlag.SYN:
			packet.setRelativeSeq(0);
			break;
		case TcpFlag.SYN + TcpFlag.ACK:
			packet.setRelativeSeq(0);
			packet.setRelativeAck(1);
			break;
		default:
			if (packet.getDirection() == TcpDirection.ToServer) {
				packet.setRelativeSeq(retRelativeClientSeq(packet.getSeq()));
				packet.setRelativeAck(retRelativeServerSeq(packet.getAck()));

			} else {
				packet.setRelativeSeq(retRelativeServerSeq(packet.getSeq()));
				packet.setRelativeAck(retRelativeClientSeq(packet.getAck()));
			}
			break;
		}
	}
}