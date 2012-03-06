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
package org.krakenapps.pcap.live;

/**
 * The structure keeps statistics values on an interface.
 * 
 * @author delmitz
 */
public class PcapStat {
	private int recv;
	private int drop;
	private int ifdrop;

	public PcapStat(int recv, int drop, int ifdrop) {
		this.recv = recv;
		this.drop = drop;
		this.ifdrop = ifdrop;
	}

	/**
	 * Returns the number of packets transited on the network.
	 * 
	 * @return the number of packets transited on the network
	 */
	public long getCapturedPackets() {
		return recv;
	}

	/**
	 * Returns the number of packets dropped by the driver.
	 * 
	 * @return the number of packets dropped by the driver
	 */
	public long getDroppedPackets() {
		return drop;
	}

	/**
	 * Returns the number of dropped packets by interface.
	 * 
	 * @return the number of dropped packets by interface
	 */
	public long getInterfaceDroppedPackets() {
		return ifdrop;
	}

	@Override
	public String toString() {
		return "recv=" + recv + ", drop=" + drop + ", ifdrop=" + ifdrop;
	}
}
