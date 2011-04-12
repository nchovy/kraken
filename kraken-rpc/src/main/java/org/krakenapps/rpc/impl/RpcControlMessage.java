package org.krakenapps.rpc.impl;

import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcMessage;

public class RpcControlMessage {
	private RpcControlMessage() {
	}

	public static RpcMessage createPeerRequest(RpcConnection conn, String guid) {
		return RpcMessage.newCall(conn.nextMessageId(), 0, "peering-request", new Object[] { guid, true });
	}

	public static RpcMessage createAuthenticateRequest(RpcConnection conn, String guid, String hash) {
		return RpcMessage.newCall(conn.nextMessageId(), 0, "authenticate", new Object[] { guid, hash });
	}
}
