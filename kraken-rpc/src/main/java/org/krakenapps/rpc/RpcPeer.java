package org.krakenapps.rpc;

import org.krakenapps.rpc.impl.RpcTrustLevel;

public interface RpcPeer {
	String getGuid();
	
	String getPassword();
	
	RpcTrustLevel getTrustLevel();
}
