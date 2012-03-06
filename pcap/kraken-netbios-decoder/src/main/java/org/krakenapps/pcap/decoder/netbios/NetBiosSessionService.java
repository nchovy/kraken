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
