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
package org.krakenapps.pcap.decoder.rpce.packet;

import org.krakenapps.pcap.decoder.rpce.RpcUdpHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class UdpCancelAckPDU implements UdpPDUInterface{

	private int vers; // it must 0x00
	private int cancelID;
	boolean serverIsAccepting; // true is accepting

	public int getVers() {
		return vers;
	}

	public void setVers(int vers) {
		this.vers = vers;
	}

	public int getCancelID() {
		return cancelID;
	}

	public void setCancelID(int cancelId) {
		this.cancelID = cancelId;
	}

	public boolean isServerIsAccepting() {
		return serverIsAccepting;
	}

	public void setServerIsAccepting(boolean serverIsAccepting) {
		this.serverIsAccepting = serverIsAccepting;
	}

	@Override
	public void parse(Buffer b, RpcUdpHeader h) {
		vers = ByteOrderConverter.swap(b.getInt());
		cancelID = ByteOrderConverter.swap(b.getInt());
	}
}
