package org.krakenapps.honey.sshd.impl;

import java.io.File;
import java.io.IOException;

import org.apache.sshd.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.krakenapps.honey.sshd.HoneySshService;

public class HoneySshServiceImpl implements HoneySshService {
	private String hostname;
	private File rootPath;
	private SshServer sshd;

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
			throw new IllegalArgumentException("dir does not exist");

		if (!dir.isDirectory())
			throw new IllegalArgumentException("dir should be directory");

		this.rootPath = dir;
	}

	@Override
	public void open() throws IOException {
		SshServer sshd = SshServer.setUpDefaultServer();
		sshd.setPort(22);
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.pem"));
		sshd.setShellFactory(new HoneySshCommandFactory(this));
		sshd.setPasswordAuthenticator(new HoneyPasswordAuthenticator());
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

	public static void main(String[] args) throws IOException {
		HoneySshServiceImpl s = new HoneySshServiceImpl();
		s.setRootPath(new File("src/main/resources/fakefs"));
		s.open();
	}
}
