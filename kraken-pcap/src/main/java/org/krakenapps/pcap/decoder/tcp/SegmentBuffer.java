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

import java.util.Collection;

import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindori
 */
public class SegmentBuffer {
	private static final int FIN_NUMBER = 1;
	private static final int ACK_NUMBER = 16;
	private static final int FIN_ACK_NUMBER = 17;
	private static final int PSH_ACK_NUMBER = 24;
	private static final int FIN_PSH_ACK_NUMBER = 25;

	private final Logger logger = LoggerFactory.getLogger(SegmentBuffer.class.getName());

	private int currentSeq; // Last packet's sequence number.
	private int currentAck; // Last packet's acknowledgment number.
	private int currentDataLength; // Last packet's data length.

	private Buffer buffer;
	private TcpProtocolMapper mapper;

	public SegmentBuffer(TcpProtocolMapper mapper) {
		this.mapper = mapper;
		buffer = new ChainBuffer();
	}

	private void setCurrentVar(int currentSeqNumber, int currentAckNumber, int currentDataLength) {
		this.currentSeq = currentSeqNumber;
		this.currentAck = currentAckNumber;
		this.currentDataLength = currentDataLength;
	}

	public int getCurrentSeq() {
		return currentSeq;
	}

	public int getCurrentAck() {
		return currentAck;
	}

	public int getCurrentDataLength() {
		return currentDataLength;
	}

	public void initTxBuffer(TcpPacket segment) { // call time : Arrive SYN
		int seqNum = segment.getRelativeSeq();
		setCurrentVar(seqNum, 0, 1);
	}

	public void initRxBuffer(TcpPacket segment) { // call time : Arrive
		// SYN-ACK
		int seqNum = segment.getRelativeSeq();
		int ackNum = segment.getRelativeAck();
		setCurrentVar(seqNum, ackNum, 1); // 1 is SYN Flag length.
	}

	public void store(TcpSessionImpl session, WaitQueue waitQueue, TcpPacket segment,
			boolean flushBit) {
		Buffer data = segment.getData();
		int finLen = 0;

		if (isValidSeq(segment) == true) {
			if (segment.getFlags() == FIN_NUMBER
					|| segment.getFlags() == FIN_ACK_NUMBER
					|| segment.getFlags() == FIN_PSH_ACK_NUMBER)
				finLen += 1;
			if (data != null) {
				setCurrentVar(segment.getRelativeSeq(), segment.getRelativeAck(), (segment
						.getDataLength() + finLen));
				push(session, segment);
				if (flushBit == true)
					flush(session, segment);

				processNextPacket(session, waitQueue, segment);
			} else {
				setCurrentVar(segment.getRelativeSeq(), segment.getRelativeAck(), finLen);
				if (flushBit == true)
					flush(session, segment);
				processNextPacket(session, waitQueue, segment);
			}
			setLastAck(session, segment);
		} else {
			waitQueue.enqueue(segment);
		}
	}

	private void flush(TcpSessionImpl session, TcpPacket segment) {
		logger.debug("tcp segment buffer: flush to buffer");
		fireCallbacks(session, segment);
		buffer = null;
	}

	private boolean isValidSeq(TcpPacket segment) {
		int seqNum = segment.getRelativeSeq();
		if ((getCurrentSeq() + getCurrentDataLength()) == seqNum)
			return true;
		else
			return false;
	}

	public TcpPacket retrieveNextPacket(TcpDecoder tcp, TcpSessionImpl session,
			WaitQueue waitQueue) {
		TcpPacket element = retrieveWaitQueue(session, waitQueue);
		int finLen = 0;

		while (element != null) {
			if (element.getRelativeSeq() == getCurrentSeq() && element.getData() != null) {
				if (getCurrentDataLength() != 0 && element.getDataLength() > getCurrentDataLength()) {
					/*
					 * Case: TCP Retransmission If, current packet have added
					 * data (ex. before packet 3396-3500, after packet
					 * 3396-3654)
					 */

					int beforeDataLength = getCurrentDataLength();
					setCurrentVar(element.getRelativeSeq(), element.getRelativeAck(), (element
							.getDataLength() + finLen));
					Buffer data = element.getData();
					byte[] addedData = new byte[element.getDataLength() - beforeDataLength];
					/* TODO: removed temp, and replace move to buffer's offset */
					byte[] temp = new byte[beforeDataLength];
					data.gets(temp, 0, beforeDataLength);
					data.gets(addedData, 0, addedData.length);
					
					Protocol protocol = session.getProtocol();
					Collection<TcpProcessor> p = mapper.getTcpProcessors(protocol);
					
					/* case: find the undefined protocol(e.g. DAAP) */
					if(p == null)
						return element;
					
					for(TcpProcessor p1: p) {
						if(p1 == null)
							continue;
						handlingL7(session.getKey(), p1, element.getDirection(), addedData);
					}
					return element;
				}
			}
			if (element.getFlags() == FIN_NUMBER
					|| element.getFlags() == FIN_ACK_NUMBER
					|| element.getFlags() == FIN_PSH_ACK_NUMBER)
				finLen += 1;
			if (element.getData() == null) {
				setCurrentVar(element.getRelativeSeq(), element.getRelativeAck(), finLen);
				if (element.getFlags() == ACK_NUMBER
						|| element.getFlags() == FIN_NUMBER
						|| element.getFlags() == FIN_ACK_NUMBER) {
					setLastAck(session, element);
					return element;
				}
			} else {
				setCurrentVar(element.getRelativeSeq(), element.getRelativeAck(), (element
						.getDataLength() + finLen));
				setLastAck(session, element);
				if (element.getFlags() == PSH_ACK_NUMBER
						|| element.getFlags() == FIN_NUMBER
						|| element.getFlags() == FIN_ACK_NUMBER
						|| element.getFlags() == FIN_PSH_ACK_NUMBER) {
					push(session, element);
					flush(session, element);
					// return element; // Notify to transitState.
				} else {
					push(session, element);
				}
			}
			element = retrieveWaitQueue(session, waitQueue);
		}
		return null;
	}

	private void processNextPacket(TcpSessionImpl session, WaitQueue waitQueue,
			TcpPacket segment) {
		TcpPacket element = retrieveWaitQueue(session, waitQueue);
		int finLen = 0;
		while (element != null) {
			int control = segment.getFlags();
			if (control == FIN_NUMBER || control == FIN_ACK_NUMBER || control == FIN_PSH_ACK_NUMBER)
				finLen += 1;
			setCurrentVar(element.getRelativeSeq(), element.getRelativeAck(), (element
					.getDataLength() + finLen));
			if (element.getData() != null) {
				if (control == PSH_ACK_NUMBER || control == FIN_PSH_ACK_NUMBER) {
					push(session, element);
					flush(session, element);
				} else {
					push(session, element);
				}
			}
			element = retrieveWaitQueue(session, waitQueue);
		}
	}

	private TcpPacket retrieveWaitQueue(TcpSessionImpl session, WaitQueue waitQueue) {
		int size = waitQueue.size();
		int expectedSeq = getCurrentSeq() + getCurrentDataLength();
		
		for (int i = 0; i < size; i++) {
			if (waitQueue.dequeue(i) == null)
				continue;

			TcpPacket element = waitQueue.dequeue(i);
			if (element.getRelativeSeq() == getCurrentSeq() && element.getData() != null
					&& element.getDataLength() > getCurrentDataLength()) {
				/*
				 * Case: TCP Retransmission (ex. before packet 3396-3500, after
				 * packet 3396-3923) => added data.
				 */
				return element;
			}

			if (element.getRelativeSeq() == expectedSeq) {
				if (element.getDirection() == TcpDirection.ToServer) {
					waitQueue.remove(i);
					return element;
				} else {
					waitQueue.remove(i);
					return element;
				}
			}
		}
		return null;
	}

	public void fireCallbacks(TcpSessionImpl session, TcpPacket segment) {
		if (buffer != null) {
			int remain = buffer.readableBytes();
			if (remain == 0) {
				return;
			}

			Protocol protocol = session.getProtocol();
			Collection<TcpProcessor> processors = mapper.getTcpProcessors(protocol);
			
			if(processors == null)
				return;
			
			for(TcpProcessor p: processors) {
				handlingL7(session.getKey(), p, segment.getDirection(), buffer);
			}
		}
	}

	private void setLastAck(TcpSessionImpl session, TcpPacket segment) {
		if (segment.getDirection() == TcpDirection.ToServer) {
			session.setSrcLastAck(segment.getRelativeAck());
			if (segment.getData() != null)
				session.setRecentSrc(segment.getRelativeSeq(), segment.getRelativeAck(), segment
						.getDataLength(), (int) segment.getFlags());
			else
				session.setRecentSrc(segment.getRelativeSeq(), segment.getRelativeAck(), 0,
						(int) segment.getFlags());
		}

		else {
			session.setDestLastAck(segment.getRelativeAck());
			if (segment.getData() != null)
				session.setRecentDest(segment.getRelativeSeq(), segment.getRelativeAck(), segment
						.getDataLength(), (int) segment.getFlags());
			else
				session.setRecentDest(segment.getRelativeSeq(), segment.getRelativeAck(), 0,
						(int) segment.getFlags());
		}
	}

	private void push(TcpSessionImpl session, TcpPacket segment) {
		if (buffer == null) {
			buffer = new ChainBuffer();
		}
		
		session.removePacket(segment);
		buffer.addLast(segment.getData(), segment.getDataLength());
	}
	
	private void handlingL7(TcpSessionKey key, TcpProcessor processor, TcpDirection direction, Buffer data) {
		if (direction == TcpDirection.ToServer)
			processor.handleTx(key, data);
		else
			processor.handleRx(key, data);
	}
	
	private void handlingL7(TcpSessionKey key, TcpProcessor processor, TcpDirection direction, byte[] data) {
		Buffer newData = new ChainBuffer();
		newData.addLast(data);
		
		if (direction == TcpDirection.ToServer)
			processor.handleTx(key, newData);
		else
			processor.handleRx(key, newData);
	}
}