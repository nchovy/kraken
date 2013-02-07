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
package org.krakenapps.pcap.decoder.rpce;

import org.krakenapps.pcap.decoder.rpce.rr.TcpPDUType;
import org.krakenapps.pcap.decoder.rpce.tcppacket.association.TcpAlterContextPDU;
import org.krakenapps.pcap.decoder.rpce.tcppacket.association.TcpAlterContextResponsePDU;
import org.krakenapps.pcap.decoder.rpce.tcppacket.association.TcpBindAckPDU;
import org.krakenapps.pcap.decoder.rpce.tcppacket.association.TcpBindNakPDU;
import org.krakenapps.pcap.decoder.rpce.tcppacket.association.TcpBindPDU;
import org.krakenapps.pcap.decoder.rpce.tcppacket.association.TcpPDUInterface;
import org.krakenapps.pcap.decoder.rpce.tcppacket.call.TcpCancel;
import org.krakenapps.pcap.decoder.rpce.tcppacket.call.TcpFault;
import org.krakenapps.pcap.decoder.rpce.tcppacket.call.TcpOrphaned;
import org.krakenapps.pcap.decoder.rpce.tcppacket.call.TcpShutdown;
import org.krakenapps.pcap.decoder.rpce.tcppacket.call.TcpRequest;
import org.krakenapps.pcap.decoder.rpce.tcppacket.call.TcpResponse;

public class TcpPacketTypeMapper {

	public TcpPDUInterface getPDU(TcpPDUType type){
		
			switch(type){
				case RESPONSE :
					return new TcpResponse();
				case FAULT :
					return  new TcpFault();
				case BIND_ACK :
					return new TcpBindAckPDU();
				case BIND_NACK :
					return new TcpBindNakPDU();
				case ALTER_CONTEXT_RESP :
					return new TcpAlterContextResponsePDU();
				case SHUTDOWN :
					return new TcpShutdown();
				case CO_CANCAL :
					return new TcpCancel();
				case ORPHANED :
					return new TcpOrphaned();
				case REQUEST :
					return new TcpRequest();
				case BIND :
					return new TcpBindPDU();
				case ALTER_CONTEXT :
					return new TcpAlterContextPDU();
				default :
					new IllegalAccessException(this+" : invalid Packet Type");
					return null;
		}
	}
}
