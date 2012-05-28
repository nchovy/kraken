/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.radius.server.impl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;

import org.krakenapps.radius.protocol.AccessRequest;
import org.krakenapps.radius.protocol.RadiusPacket;
import org.krakenapps.radius.server.RadiusAuthenticator;
import org.krakenapps.radius.server.RadiusClientAddress;
import org.krakenapps.radius.server.RadiusModule;
import org.krakenapps.radius.server.RadiusModuleType;
import org.krakenapps.radius.server.RadiusPortType;
import org.krakenapps.radius.server.RadiusProfile;
import org.krakenapps.radius.server.RadiusServer;
import org.krakenapps.radius.server.RadiusUserDatabase;
import org.krakenapps.radius.server.RadiusVirtualServer;
import org.krakenapps.radius.server.RadiusVirtualServerEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RadiusVirtualServerImpl implements RadiusVirtualServer, Runnable {
	private final Logger logger = LoggerFactory.getLogger(RadiusVirtualServerImpl.class.getName());

	public static final String PROFILE_KEY = "profile";
	public static final String PORT_TYPE_KEY = "port_type";
	public static final String HOSTNAME_KEY = "hostname";
	public static final String PORT_KEY = "port";
	public static final int DEFAULT_AUTH_PORT = 1812;
	public static final int DEFAULT_ACCT_PORT = 1813;

	private boolean isOpened;
	private String name;
	private RadiusProfile profile;
	private RadiusPortType portType;
	private RadiusServer server;
	private InetSocketAddress bindAddress;

	private DatagramSocket socket;

	private Map<RadiusClientAddress, String> clientOverrides;
	private CopyOnWriteArraySet<RadiusVirtualServerEventListener> callbacks;
	private ExecutorService executor;
	private Thread listenerThread;

	public RadiusVirtualServerImpl(RadiusServer server, String name, String profileName, RadiusPortType portType,
			ExecutorService executor) {
		this(server, name, profileName, portType, executor, null);
	}

	public RadiusVirtualServerImpl(RadiusServer server, String name, String profileName, RadiusPortType portType,
			ExecutorService executor, InetSocketAddress bindAddress) {
		this.server = server;
		this.isOpened = false;
		this.name = name;
		this.executor = executor;
		this.bindAddress = bindAddress;
		this.clientOverrides = new ConcurrentHashMap<RadiusClientAddress, String>();
		this.callbacks = new CopyOnWriteArraySet<RadiusVirtualServerEventListener>();
		this.portType = portType;
		this.profile = server.getProfile(profileName);

		verifyNotNull("name", name);
		verifyNotNull("port type", portType);
		verifyNotNull("executor", executor);
	}

	@Override
	public void run() {
		try {
			logger.info("kraken radius: virtual server [bind={}] started", bindAddress);
			while (true) {
				try {
					runonce();
				} catch (IOException e) {
					throw e;
				} catch (Exception e) {
					logger.warn("kraken radius: cannot respond", e);
				}
			}
		} catch (IOException e) {
			if (e instanceof SocketException && e.getMessage().equalsIgnoreCase("socket closed"))
				logger.trace("kraken radius: socket closed");
			else
				logger.info("kraken radius: io error", e);
		} finally {
			logger.info("kraken radius: virtual server [bind={}] stopped", bindAddress);
		}
	}

	private void runonce() throws IOException {
		byte[] buf = new byte[4096];
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		socket.receive(p);

		logger.info("kraken radius: received datagram from " + p.getSocketAddress());

		if (profile == null) {
			logger.debug("kraken radius: profile not set for virtual server [{}]", bindAddress);
			return;
		}

		RadiusPacket packet = RadiusPacket.parse(profile.getSharedSecret(), buf);
		logger.debug("kraken radius: received radius packet [{}]", packet);

		if (packet instanceof AccessRequest) {
			AccessRequest accessRequest = (AccessRequest) packet;
			List<RadiusUserDatabase> userDatabases = getUserDatabases(profile);

			for (String name : profile.getAuthenticators()) {
				RadiusAuthenticator auth = getAuthenticator(name);
				InetSocketAddress remoteAddr = (InetSocketAddress) p.getSocketAddress();
				executor.execute(new AuthTask(remoteAddr, profile, accessRequest, auth, userDatabases));
			}
		}
	}

	private List<RadiusUserDatabase> getUserDatabases(RadiusProfile profile) {
		// TODO: cache using update profile event
		List<RadiusUserDatabase> udbs = new ArrayList<RadiusUserDatabase>();
		for (String name : profile.getUserDatabases())
			udbs.add(getUserDatabase(name));
		return udbs;
	}

	private RadiusAuthenticator getAuthenticator(String name) {
		RadiusModule module = server.getModule(RadiusModuleType.Authenticator);
		return (RadiusAuthenticator) module.getInstance(name);
	}

	private RadiusUserDatabase getUserDatabase(String name) {
		RadiusModule module = server.getModule(RadiusModuleType.UserDatabase);
		return (RadiusUserDatabase) module.getInstance(name);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isOpened() {
		return isOpened;
	}

	@Override
	public void open() throws IOException {
		if (bindAddress != null)
			socket = new DatagramSocket(bindAddress.getPort(), bindAddress.getAddress());
		else {
			socket = new DatagramSocket(DEFAULT_AUTH_PORT);
			bindAddress = (InetSocketAddress) socket.getLocalSocketAddress();
		}

		listenerThread = new Thread(this, "Radius Virtual Server");
		listenerThread.start();

		this.isOpened = true;
		logger.info("kraken radius: opened virtual server [{}]", bindAddress);
	}

	@Override
	public void close() throws IOException {
		try {
			socket.close();
			executor.shutdownNow();
			listenerThread.interrupt();
			logger.info("kraken radius: closed virtual server [{}]", bindAddress);
		} finally {
			// fire callbacks
			for (RadiusVirtualServerEventListener callback : callbacks) {
				try {
					callback.onClose(this);
				} catch (Exception e) {
					logger.error("kraken radius: callback should not throw any exception", e);
				}
			}
		}
	}

	@Override
	public InetSocketAddress getBindAddress() {
		return bindAddress;
	}

	@Override
	public RadiusPortType getPortType() {
		return portType;
	}

	@Override
	public RadiusProfile getProfile() {
		return profile;
	}

	@Override
	public List<RadiusClientAddress> getOverriddenClients() {
		return new ArrayList<RadiusClientAddress>(clientOverrides.keySet());
	}

	@Override
	public RadiusProfile getClientProfile(RadiusClientAddress client) {
		String profileName = clientOverrides.get(client);
		if (profileName == null)
			return null;

		return server.getProfile(profileName);
	}

	@Override
	public void addClientProfile(RadiusClientAddress client, String profileName) {
		clientOverrides.put(client, profileName);

		// fire callbacks
		for (RadiusVirtualServerEventListener callback : callbacks) {
			try {
				callback.onAddClientProfile(this, client, profileName);
			} catch (Exception e) {
				logger.error("kraken radius: callback should not throw any exception", e);
			}
		}
	}

	@Override
	public void removeClientProfile(RadiusClientAddress client) {
		String profileName = clientOverrides.remove(client);
		if (profileName == null)
			return;

		// fire callbacks
		for (RadiusVirtualServerEventListener callback : callbacks) {
			try {
				callback.onRemoveClientProfile(this, client);
			} catch (Exception e) {
				logger.error("kraken radius: callback should not throw any exception", e);
			}
		}
	}

	@Override
	public void addEventListener(RadiusVirtualServerEventListener listener) {
		callbacks.add(listener);
	}

	@Override
	public void removeEventListener(RadiusVirtualServerEventListener listener) {
		callbacks.remove(listener);
	}

	private void verifyNotNull(String name, Object value) {
		if (value == null)
			throw new IllegalArgumentException(name + " should be not null");
	}

	private class AuthTask implements Runnable {
		private InetSocketAddress remoteAddr;
		private RadiusProfile profile;
		private AccessRequest accessRequest;
		private RadiusAuthenticator authenticator;
		private List<RadiusUserDatabase> userDatabases;

		public AuthTask(InetSocketAddress remoteAddr, RadiusProfile profile, AccessRequest accessRequest,
				RadiusAuthenticator authenticator, List<RadiusUserDatabase> userDatabases) {
			this.remoteAddr = remoteAddr;
			this.profile = profile;
			this.accessRequest = accessRequest;
			this.authenticator = authenticator;
			this.userDatabases = userDatabases;
		}

		@Override
		public void run() {
			RadiusPacket response = authenticator.authenticate(profile, accessRequest, userDatabases);
			response.finalize();

			byte[] buf = response.getBytes();

			try {
				DatagramPacket p = new DatagramPacket(buf, buf.length, remoteAddr);
				socket.send(p);
				logger.info("kraken radius: sent response [{}] to [{}]", response, remoteAddr);
			} catch (Exception e) {
				logger.error("kraken radius: cannot respond to " + remoteAddr, e);
			}
		}
	}

	@Override
	public String toString() {
		return "[" + name + "] type=" + portType.getAlias() + ", profile=" + profile.getName() + ", bind="
				+ bindAddress;
	}
}
