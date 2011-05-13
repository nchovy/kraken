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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.krakenapps.pcap.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindori
 */
public class TcpSessionImpl implements TcpSession {
	// tx : Server side, rx : Client side
	private final Logger logger = LoggerFactory.getLogger(TcpSessionImpl.class.getName());

	private static AtomicInteger LAST_ID = new AtomicInteger(1);
	private int id;

	private TcpStreamOption srcStreamOption;
	private TcpStreamOption destStreamOption;

	private TcpSessionKeyImpl key;

	private int srcFirstSeq;
	private int destFirstSeq;

	private int srcLastAck = -1;
	private int destLastAck = -1;

	private int recentSrcSeq = -1;
	private int recentSrcAck = -1;
	private int recentSrcDataLength = -1;
	private int recentSrcFlags = -1;

	private int recentDestSeq = -1;
	private int recentDestAck = -1;
	private int recentDestDataLength = -1;
	private int recentDestFlags = -1;

	private TcpState clientState;
	private TcpState serverState;

	private boolean isEstablished;
	private boolean isReceiveData = false;

	private List<TcpPacket> packetList;

	private WaitQueue txWaitQueue;
	private WaitQueue rxWaitQueue;

	private SegmentBuffer txBuffer;
	private SegmentBuffer rxBuffer;

	private FinPacketKey finPacketKey;

	private int packetCountAfterFin = 0;

	private int firstFinSeq = -1;
	private int firstFinAck = -1;

	private Protocol protocol;

	public TcpSessionImpl(TcpProtocolMapper mapper) {
		id = LAST_ID.getAndIncrement();
		srcStreamOption = TcpStreamOption.NORMAL;
		destStreamOption = TcpStreamOption.NORMAL;
		key = null;
		srcFirstSeq = -1;
		destFirstSeq = -1;
		clientState = TcpState.LISTEN;
		serverState = TcpState.LISTEN;
		packetList = new ArrayList<TcpPacket>();
		txWaitQueue = new WaitQueue();
		rxWaitQueue = new WaitQueue();
		txBuffer = new SegmentBuffer(mapper);
		rxBuffer = new SegmentBuffer(mapper);
		finPacketKey = new FinPacketKey();
	}

	@Override
	public int getId() {
		return id;
	}

	public boolean isEstablished() {
		return isEstablished;
	}

	public void setEstablished(boolean isEstablished) {
		this.isEstablished = isEstablished;
	}

	public boolean isReceiveData() {
		return isReceiveData;
	}

	public void setReceiveData(boolean isReceiveData) {
		this.isReceiveData = isReceiveData;
	}

	public TcpStreamOption getSourceOption() {
		return srcStreamOption;
	}

	public void setSrcStreamOption(TcpStreamOption srcStreamOption) {
		this.srcStreamOption = srcStreamOption;
	}

	public TcpStreamOption getDestinationOption() {
		return destStreamOption;
	}

	public void setDestStreamOption(TcpStreamOption destStreamOption) {
		this.destStreamOption = destStreamOption;
	}

	@Override
	public TcpSessionKey getKey() {
		return key;
	}

	public void setTcpSessionKey(TcpSessionKeyImpl key) {
		this.key = key;
	}

	public int getSrcFirstSeq() {
		return srcFirstSeq;
	}

	public void setSrcFirstSeq(int firstSeq) {
		srcFirstSeq = firstSeq;
	}

	public int getDestFirstSeq() {
		return destFirstSeq;
	}

	public void setDestFirstSeq(int firstSeq) {
		destFirstSeq = firstSeq;
	}

	public int getSrcLastAck() {
		return srcLastAck;
	}

	public void setSrcLastAck(int srcLastAck) {
		this.srcLastAck = srcLastAck;
	}

	public int getDestLastAck() {
		return destLastAck;
	}

	public void setDestLastAck(int destLastAck) {
		this.destLastAck = destLastAck;
	}

	public int getRecentSrcSeq() {
		return recentSrcSeq;
	}

	public int getRecentSrcAck() {
		return recentSrcAck;
	}

	public int getRecentSrcDataLength() {
		return recentSrcDataLength;
	}

	public int getRecentSrcFlags() {
		return recentSrcFlags;
	}

	public void setRecentSrc(int seq, int ack, int dataLen, int flags) {
		recentSrcSeq = seq;
		recentSrcAck = ack;
		recentSrcDataLength = dataLen;
		recentSrcFlags = flags;
	}

	public int getRecentDestSeq() {
		return recentDestSeq;
	}

	public int getRecentDestAck() {
		return recentDestAck;
	}

	public int getRecentDestDataLength() {
		return recentDestDataLength;
	}

	public int getRecentDestFlags() {
		return recentDestFlags;
	}

	public void setRecentDest(int seq, int ack, int dataLen, int flags) {
		recentDestSeq = seq;
		recentDestAck = ack;
		recentDestDataLength = dataLen;
		recentDestFlags = flags;
	}

	@Override
	public TcpState getClientState() {
		return clientState;
	}

	public void setClientState(TcpState clientState) {
		this.clientState = clientState;
	}

	@Override
	public TcpState getServerState() {
		return serverState;
	}

	public void setServerState(TcpState serverState) {
		this.serverState = serverState;
	}

	public int retRelativeSrcSeq(int sequenceNumber) {
		return (sequenceNumber - srcFirstSeq);
	}

	public int retRelativeDestSeq(int sequenceNumber) {
		return (sequenceNumber - destFirstSeq);
	}

	public List<TcpPacket> getPacketList() {
		return packetList;
	}

	public void addPacket(TcpPacket packet) {
		packetList.add(packet);
	}

	public void removePacket(TcpPacket packet) {
		if (packetList.contains(packet))
			packetList.remove(packet);
	}

	public void clearPacketList() {
		packetList.removeAll(packetList);
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

	public WaitQueue getTxWaitQueue() {
		return txWaitQueue;
	}

	public WaitQueue getRxWaitQueue() {
		return rxWaitQueue;
	}

	public SegmentBuffer getTxBuffer() {
		return txBuffer;
	}

	public SegmentBuffer getRxBuffer() {
		return rxBuffer;
	}

	public FinPacketKey getFinPacketKey() {
		return finPacketKey;
	}

	public void deallocateTxBuffer() {
		txBuffer = null;
	}

	public void deallocateRxBuffer() {
		rxBuffer = null;
	}

	@Override
	public void registerProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	@Override
	public void unregisterProtocol(Protocol protocol) {
		if (this.protocol == protocol)
			this.protocol = null;
	}

	@Override
	public Protocol getProtocol() {
		return protocol;
	}

	@Override
	public String toString() {
		InetSocketAddress clientAddress = new InetSocketAddress(key.getClientIp(), key.getClientPort());
		InetSocketAddress serverAddress = new InetSocketAddress(key.getServerIp(), key.getServerPort());

		return String.format("[%s (%s) => %s (%s)] rx=%d (%d), tx=%d (%d)", clientAddress, clientState, serverAddress,
				serverState, recentDestSeq, recentDestAck, recentSrcSeq, recentSrcAck);
	}

	public void printState() {
		System.out.println("Client state: " + clientState + "\n" + "Server state: " + serverState);
		if (logger.isDebugEnabled())
			logger.debug("Client state: " + clientState + "\n" + "Server state: " + serverState);
	}
}
