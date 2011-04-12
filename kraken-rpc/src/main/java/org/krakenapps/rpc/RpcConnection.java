package org.krakenapps.rpc;

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Properties;

import org.krakenapps.rpc.impl.RpcTrustLevel;

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

	boolean requestPeering();

	boolean requestPeering(String password);

	void addListener(RpcConnectionEventListener callback);

	void removeListener(RpcConnectionEventListener callback);

	RpcBlockingTable getBlockingTable();

	void waitControlReady();
	
	void waitPeering();
	
	void setControlReady();
}
