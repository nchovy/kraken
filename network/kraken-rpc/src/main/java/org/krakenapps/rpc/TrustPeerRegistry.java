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
package org.krakenapps.rpc;

import java.util.ArrayList;
import java.util.Collection;

import org.krakenapps.rpc.impl.RpcPeerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrustPeerRegistry implements RpcPeerRegistry {
	private final Logger logger = LoggerFactory.getLogger(TrustPeerRegistry.class.getName());

	@Override
	public Collection<String> getPeerGuids() {
		return new ArrayList<String>();
	}

	@Override
	public RpcPeer authenticate(String guid, String nonce, String hash) {
		// trust all password hash
		logger.trace("kraken-rpc: bypass auth for [guid={}, nonce={}, hash={}]", new Object[] { guid, nonce, hash });
		return new RpcPeerConfig(guid, null, RpcTrustLevel.High.getCode());
	}

	@Override
	public RpcPeer findPeer(String guid) {
		return null;
	}

	@Override
	public void register(RpcPeer peer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void unregister(String guid) {
		throw new UnsupportedOperationException();
	}
}
