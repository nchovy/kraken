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
