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
package org.krakenapps.honey.sshd.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.krakenapps.honey.sshd.HoneyLoginAttemptListener;
import org.krakenapps.honey.sshd.HoneySshService;

@Component(name = "honey-sshd")
@Provides
public class HoneySshServiceImpl implements HoneySshService {
	private String hostname;
	private File rootPath;
	private SshServer sshd;

	private CopyOnWriteArraySet<HoneyLoginAttemptListener> loginAttemptListeners;

	public HoneySshServiceImpl() {
		loginAttemptListeners = new CopyOnWriteArraySet<HoneyLoginAttemptListener>();
	}

	@Invalidate
	public void stop() {
		close();
	}

	@Override
	public String getHostname() {
		if (hostname == null)
			return "localhost";
		return hostname;
	}

	@Override
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	@Override
	public File getRootPath() {
		return rootPath;
	}

	@Override
	public void setRootPath(File dir) {
		if (dir == null)
			throw new IllegalArgumentException("dir should be not null");

		if (!dir.exists())
			throw new IllegalArgumentException("dir does not exist: " + dir.getAbsolutePath());

		if (!dir.isDirectory())
			throw new IllegalArgumentException("dir should be directory: " + dir.getAbsolutePath());

		this.rootPath = dir;
	}

	@Override
	public void open() throws IOException {
		SshServer sshd = SshServer.setUpDefaultServer();
		sshd.setPort(22);
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.pem"));
		sshd.setShellFactory(new HoneySshCommandFactory(this));
		sshd.setPasswordAuthenticator(new HoneyPasswordAuthenticator(this));
		sshd.start();
	}

	@Override
	public void close() {
		if (sshd != null)
			try {
				sshd.stop(true);
			} catch (InterruptedException e) {
			}
	}

	@Override
	public Collection<HoneyLoginAttemptListener> getLoginAttemptListeners() {
		return Collections.unmodifiableCollection(loginAttemptListeners);
	}

	@Override
	public void addLoginAttemptListener(HoneyLoginAttemptListener listener) {
		loginAttemptListeners.add(listener);
	}

	@Override
	public void removeLoginAttemptListener(HoneyLoginAttemptListener listener) {
		loginAttemptListeners.remove(listener);
	}

	public static void main(String[] args) throws IOException {
		HoneySshServiceImpl s = new HoneySshServiceImpl();
		// s.setRootPath(new File("src/main/resources/fakefs"));
		s.open();
	}
}
