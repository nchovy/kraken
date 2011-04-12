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

/**
 * @author mindori
 */
public class FinPacketKey {
	private int currentSeq;

	private int currentAck;

	private int currentDataLength;

	private TcpDirection segmentDirection;

	public FinPacketKey() {
		setMemberVars(-1, -1, -1, null);
	}

	public FinPacketKey(int firstSeqNumber, int firstAckNumber, int firstDataLength, TcpDirection segmentDirection) {
		setMemberVars(firstSeqNumber, firstAckNumber, firstDataLength, segmentDirection);
	}

	public FinPacketKey(FinPacketKey other) {
		// copy constructor
		setMemberVars(other.getCurrentSeq(), other.getCurrentAck(), other.getCurrentDataLength(), other
				.getSegmentDirection());
	}

	public void setMemberVars(int firstSeqNumber, int firstAckNumber, int firstDataLength, TcpDirection segmentDirection) {
		this.currentSeq = firstSeqNumber;
		this.currentAck = firstAckNumber;
		this.currentDataLength = firstDataLength;
		this.segmentDirection = segmentDirection;
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

	public TcpDirection getSegmentDirection() {
		return segmentDirection;
	}
}
