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

import org.krakenapps.confdb.CollectionName;
import org.krakenapps.rpc.RpcPeer;
import org.krakenapps.rpc.RpcTrustLevel;

@CollectionName("peers")
public class RpcPeerConfig implements RpcPeer {
	private String guid;
	private String password; // hash
	private RpcTrustLevel trustLevel;

	public RpcPeerConfig() {
	}

	public RpcPeerConfig(String guid, String password, int trustLevel) {
		this.guid = guid;
		this.password = password;
		this.trustLevel = RpcTrustLevel.parse(trustLevel);
	}

	@Override
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public RpcTrustLevel getTrustLevel() {
		return trustLevel;
	}

	public void setTrustLevel(RpcTrustLevel trustLevel) {
		this.trustLevel = trustLevel;
	}

}
