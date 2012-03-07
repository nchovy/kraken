package org.krakenapps.base;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.krakenapps.log.api.Logger;
import org.krakenapps.rpc.RpcAsyncCallback;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcSession;

public interface SentryProxy {
	boolean isOpen();

	String getGuid();

	Object call(String method, Object[] params) throws RpcException, InterruptedException;

	Object call(String method, Object[] params, long timeout) throws RpcException, InterruptedException;

	void call(String method, Object[] params, RpcAsyncCallback callback);

	void post(String method, Object[] params);

	void requestLogChannel();

	RpcSession getLogSession();

	void setLogSession(String nonce, RpcSession logSession);

	Map<String, RemoteLoggerFactoryInfo> getRemoteLoggerFactories() throws RpcException, InterruptedException;

	RemoteLoggerFactoryInfo getRemoteLoggerFactory(String name) throws RpcException, InterruptedException;

	Map<String, Logger> getRemoteLoggers() throws RpcException, InterruptedException;

	Logger createRemoteLogger(String factoryName, String name, String description, Properties props)
			throws RpcException, InterruptedException;

	void removeRemoteLogger(String name) throws RpcException, InterruptedException;

	void startRemoteLogger(String name, int interval) throws RpcException, InterruptedException;

	void stopRemoteLogger(String name, int timeout) throws RpcException, InterruptedException;

	void connectRemoteLogger(String loggerFullName) throws RpcException, InterruptedException;

	void disconnectRemoteLogger(String loggerFullName) throws RpcException, InterruptedException;

	Collection<Logger> getConnectedLoggers();

	Logger getConnectedLogger(String name);

	void syncLoggerFactories() throws RpcException, InterruptedException;

	void syncLoggers() throws RpcException, InterruptedException;

	void close();

	void registerLoggerFactory(RemoteLoggerFactoryInfo factory);

	void unregisterLoggerFactory(String factoryFullName);

	void registerLogger(Logger logger);

	void unregisterLogger(String loggerFullName);

	void loggerStarted(String loggerFullName, int interval);

	void loggerStopped(String loggerFullName);
}
