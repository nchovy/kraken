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

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Properties;


public interface RpcConnection {
	/**
	 * @return the connection id
	 */
	int getId();

	boolean isOpen();

	/**
	 * @return true if this connection is created by connect() call.
	 */
	boolean isClient();

	RpcTrustLevel getTrustedLevel();

	/**
	 * Set true if peer sends challenge success message.
	 * 
	 * @param authorized
	 */
	void setTrustedLevel(RpcTrustLevel level);

	String getPeerGuid();

	void setPeerGuid(String peerGuid);

	X509Certificate getPeerCertificate();

	String getNonce();

	void setNonce(String nonce);

	RpcTrustLevel getTrustLevel();

	void setTrustLevel(RpcTrustLevel trustLevel);

	RpcConnectionState getState();

	InetSocketAddress getRemoteAddress();

	RpcServiceBinding findServiceBinding(String serviceName);

	/**
	 * Get local service bindings.
	 */
	Collection<RpcServiceBinding> getServiceBindings();

	void bind(String name, RpcService service);

	void unbind(String name);

	int nextMessageId();

	int nextSessionId();

	RpcSession createSession(String serviceName) throws RpcException, InterruptedException;

	RpcSession createSession(String serviceName, Properties props) throws RpcException, InterruptedException;

	RpcSession findSession(int sessionId);

	Collection<RpcSession> getSessions();

	RpcSession getSession(int id);

	/**
	 * Sends raw rpc message.
	 * 
	 * @param msg
	 *            the rpc message
	 */
	void send(RpcMessage msg);

	/**
	 * Closes the connection and all related resources (e.g. session).
	 */
	void close();

	void setProperty(String name, Object value);

	Object getProperty(String name);

	Collection<String> getPropertyKeys();

	void requestPeering(RpcPeeringCallback callback);

	void requestPeering(RpcPeeringCallback callback, String password);

	void addListener(RpcConnectionEventListener callback);

	void removeListener(RpcConnectionEventListener callback);

	RpcBlockingTable getBlockingTable();
	
	RpcAsyncTable getAsyncTable();

	void waitControlReady();
	
	void waitPeering();
	
	void waitPeering(long timeout);
	
	void setControlReady();
}
