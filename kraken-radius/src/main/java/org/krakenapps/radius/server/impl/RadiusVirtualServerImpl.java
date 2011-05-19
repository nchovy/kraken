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
import org.krakenapps.radius.server.RadiusConfigurator;
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
	private RadiusConfigurator conf;
	private ExecutorService executor;
	private Thread listenerThread;

	public RadiusVirtualServerImpl(RadiusServer server, String name, RadiusPortType portType, RadiusConfigurator conf,
			ExecutorService executor) {
		this(server, name, portType, conf, executor, null);
	}

	public RadiusVirtualServerImpl(RadiusServer server, String name, RadiusPortType portType, RadiusConfigurator conf,
			ExecutorService executor,
			InetSocketAddress bindAddress) {
		this.server = server;
		this.conf = conf;
		this.isOpened = false;
		this.name = name;
		this.executor = executor;
		this.bindAddress = bindAddress;
		this.clientOverrides = new ConcurrentHashMap<RadiusClientAddress, String>();
		this.callbacks = new CopyOnWriteArraySet<RadiusVirtualServerEventListener>();
	}

	@Override
	public void run() {
		try {
			logger.info("kraken radius: virtual server [bind={}] started", bindAddress);
			while (true) {
				runonce();
			}
		} catch (IOException e) {
			logger.info("kraken radius: io error", e);
		} finally {
			logger.info("kraken radius: virtual server [bind={}] stopped", bindAddress);
		}
	}

	private void runonce() throws IOException {
		byte[] buf = new byte[4096];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);

		if (profile == null) {
			logger.debug("kraken radius: profile not set for virtual server [{}]", bindAddress);
			return;
		}

		RadiusPacket req = RadiusPacket.parse(profile.getSharedSecret(), buf);
		if (req instanceof AccessRequest) {
			AccessRequest accessRequest = (AccessRequest) req;
			List<RadiusUserDatabase> userDatabases = getUserDatabases(profile);

			for (String name : profile.getAuthenticators()) {
				//TODO:
//				RadiusAuthenticator auth = server.getAuthenticator(name);
//				executor.execute(new AuthTask(accessRequest, auth, userDatabases));
			}
		}
	}

	private List<RadiusUserDatabase> getUserDatabases(RadiusProfile profile) {
		// TODO: cache using update profile event
		List<RadiusUserDatabase> udbs = new ArrayList<RadiusUserDatabase>();
//		for (String name : profile.getUserDatabases())
//			udbs.add(server.getUserDatabase(name));
		return udbs;
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
		else
			socket = new DatagramSocket(DEFAULT_AUTH_PORT);

		listenerThread = new Thread(this, "Radius Virtual Server");
		listenerThread.start();

		this.isOpened = true;
	}

	@Override
	public void close() throws IOException {
		try {
			socket.close();
			listenerThread.interrupt();
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
	public void setProfile(String profileName) {
		if (profileName == null)
			throw new IllegalArgumentException("profile name should be not null");

		this.profile = server.getProfile(profileName);
		if (profile == null)
			throw new IllegalStateException("profile not found: " + profileName);

		// make persistent
		conf.put(PROFILE_KEY, profileName);

		// fire callbacks
		for (RadiusVirtualServerEventListener callback : callbacks) {
			try {
				callback.onSetProfile(this, profileName);
			} catch (Exception e) {
				logger.error("kraken radius: callback should not throw any exception", e);
			}
		}
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

	private static class AuthTask implements Runnable {
		private AccessRequest accessRequest;
		private RadiusAuthenticator authenticator;
		private List<RadiusUserDatabase> userDatabases;

		public AuthTask(AccessRequest accessRequest, RadiusAuthenticator authenticator,
				List<RadiusUserDatabase> userDatabases) {
			this.accessRequest = accessRequest;
			this.authenticator = authenticator;
			this.userDatabases = userDatabases;
		}

		@Override
		public void run() {
			authenticator.authenticate(accessRequest, userDatabases);
		}
	}
}
