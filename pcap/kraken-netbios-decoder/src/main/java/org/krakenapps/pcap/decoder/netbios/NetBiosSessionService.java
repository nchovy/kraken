/*
 * Copyright 2011 Future Systems, Inc
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
package org.krakenapps.pcap.decoder.netbios;

import org.krakenapps.pcap.decoder.netbios.rr.SessionKeepAlive;
import org.krakenapps.pcap.decoder.netbios.rr.SessionMessage;
import org.krakenapps.pcap.decoder.netbios.rr.SessionNegativeResponse;
import org.krakenapps.pcap.decoder.netbios.rr.SessionPositiveResponse;
import org.krakenapps.pcap.decoder.netbios.rr.SessionReassembledData;
import org.krakenapps.pcap.decoder.netbios.rr.SessionRequest;
import org.krakenapps.pcap.decoder.netbios.rr.SessionRetargetResponse;
import org.krakenapps.pcap.util.Buffer;

public class NetBiosSessionService {
	public NetBiosSessionPacket parseTx(Buffer b) {
		NetBiosSessionHeader header = NetBiosSessionHeader.parse(b);
		NetBiosSessionData data = null;

		switch (header.getType()) {
		case SessionMessage:
			data = SessionMessage.parse(b);
			break;
		case SessionRequest:
			data = SessionRequest.parse(b);
			break;
		case SessionKeepAlive:
			data = SessionKeepAlive.parse(b);
			break;
		case SessionReassembled:
			data = SessionReassembledData.parse(b);
			break;
		default:
			throw new IllegalStateException("invalid tx type: " + header.getType());
		}

		return new NetBiosSessionPacket(header, data);
	}

	public NetBiosSessionPacket parseRx(Buffer b) {
		NetBiosSessionHeader header = NetBiosSessionHeader.parse(b);
		NetBiosSessionData data = null;

		switch (header.getType()) {
		case PositiveSessionResponse: // positive session response
			data = SessionPositiveResponse.parse(b);
			break;
		case NegativeSessionResponse:
			data = SessionNegativeResponse.parse(b);
			break;
		case RetargetSessionResponse:
			data = SessionRetargetResponse.parse(b);
			break;
		case SessionKeepAlive:
			data = SessionKeepAlive.parse(b);
			break;
		case SessionMessage:
			data = SessionMessage.parse(b);
			break;
		case SessionReassembled:
			data = SessionReassembledData.parse(b);
			break;
		default:
			throw new IllegalStateException("invalid rx type: " + header.getType());
		}

		return new NetBiosSessionPacket(header, data);
	}
}
