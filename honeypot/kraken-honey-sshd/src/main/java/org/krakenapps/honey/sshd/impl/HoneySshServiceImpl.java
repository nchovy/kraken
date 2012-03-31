package org.krakenapps.honey.sshd.impl;

import java.io.IOException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.krakenapps.honey.sshd.HoneySshService;

@Component(name = "honey-sshd")
@Provides
public class HoneySshServiceImpl implements HoneySshService {
	private String hostname;
	private SshServer sshd;

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
		new HoneySshServiceImpl().open();
	}
}
