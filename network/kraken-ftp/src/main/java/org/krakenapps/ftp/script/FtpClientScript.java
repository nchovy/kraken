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
package org.krakenapps.ftp.script;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.ftp.FtpClient;
import org.krakenapps.ftp.FtpConnectProfile;
import org.krakenapps.ftp.FtpProfileService;
import org.krakenapps.ftp.ListEntry;
import org.krakenapps.ftp.FtpClient.TransferMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpClientScript implements Script {
	private Logger logger = LoggerFactory.getLogger(FtpClientScript.class.getName());
	private ScriptContext context;
	private FtpProfileService service;

	public FtpClientScript(FtpProfileService service) {
		this.service = service;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void profiles(String[] args) {
		Collection<FtpConnectProfile> profiles = service.getProfiles();
		context.println("FTP Server Profiles");
		context.println("-----------------------");
		for (FtpConnectProfile profile : profiles)
			context.println(profile);
	}

	@ScriptUsage(description = "ftp server register", arguments = {
			@ScriptArgument(name = "name", type = "string", description = "server profile name"),
			@ScriptArgument(name = "host", type = "string", description = "host address"),
			@ScriptArgument(name = "port", type = "integer", description = "connection port", optional = true) })
	public void createServerProfile(String[] args) {
		String name = args[0];
		String host = args[1];
		int port = (args.length > 2) ? Integer.parseInt(args[2]) : 21;

		try {
			context.print("Account: ");
			String account = context.readLine();
			context.print("Password: ");
			String password = context.readPassword();

			FtpConnectProfile profile = new FtpConnectProfile(name, host, port, account, password);
			service.createProfile(profile);
			context.println("created");
		} catch (InterruptedException e) {
			context.println("interrupted");
		}
	}

	@ScriptUsage(description = "ftp server unregister", arguments = { @ScriptArgument(name = "name", type = "string", description = "server profile name") })
	public void removeServerProfile(String[] args) {
		service.removeProfile(args[0]);
		context.println("removed");
	}

	@ScriptUsage(description = "ftp command line", arguments = { @ScriptArgument(name = "server profile name", type = "string", description = "server profile") })
	public void connectProfile(String[] args) {
		FtpConnectProfile profile = service.getProfile(args[0]);
		String host = profile.getHost();
		int port = profile.getPort();
		String account = profile.getAccount();
		String password = profile.getPassword();

		try {
			FtpClient client = new FtpClient(host, port);
			if (client.login(account, password))
				communication(client);
			else
				context.println("login failed");
		} catch (UnknownHostException e) {
			context.println("unknown host");
		} catch (IOException e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "ftp command line", arguments = {
			@ScriptArgument(name = "host", type = "string", description = "host name"),
			@ScriptArgument(name = "port", type = "integer", description = "server ftp port", optional = true) })
	public void connect(String[] args) {
		String host = args[0];
		int port = (args.length > 1) ? Integer.parseInt(args[1]) : 21;

		try {
			FtpClient client = new FtpClient(host, port);

			while (true) {
				context.print("Account: ");
				String account = context.readLine();
				context.print("Password: ");
				String password = context.readPassword();
				if (client.login(account, password))
					break;
				context.println(client.getLastMessage());
			}
			communication(client);
		} catch (UnknownHostException e) {
			context.println("unknown host");
		} catch (IOException e) {
			context.println(e.getMessage());
		} catch (InterruptedException e) {
			context.println("interrupted");
		}
	}

	private void communication(FtpClient client) {
		try {
			while (client.isConnected() && client.getLastMessage().getCode() != null) {
				context.print("kraken-ftp> ");
				String input = context.readLine();
				input = input.trim();

				if (input.matches("(?i)quit"))
					break;
				else if (input.matches("(?i)syst"))
					context.println(client.system());
				else if (input.matches("(?i)ascii")) {
					client.setAscii();
					context.println(client.getLastMessage().toString());
				} else if (input.matches("(?i)binary")) {
					client.setBinary();
					context.println(client.getLastMessage().toString());
				} else if (input.matches("(?i)pwd"))
					context.println(client.printWorkingDirectory());
				else if (input.matches("(?i)cwd .+")) {
					client.changeWorkingDirectory(input.substring(4));
					context.println(client.getLastMessage().toString());
				} else if (input.matches("(?i)cdup")) {
					client.changeToParentDirectory();
					context.println(client.getLastMessage().toString());
				} else if (input.matches("(?i)list")) {
					for (String str : client.list())
						context.println(str);
				} else if (input.matches("(?i)mlsd")) {
					ListEntry[] mlsd = client.mlsd();
					if (mlsd != null) {
						for (ListEntry entry : mlsd)
							context.println(entry);
					}
				} else if (input.matches("(?i)retr .+")) {
					context.print("download directory: ");
					client.retrieve(input.substring(5), new File(context.readLine()));
					context.println(client.getLastMessage().toString());
				} else if (input.matches("(?i)stor .+")) {
					client.store(new File(input.substring(5)));
					context.println(client.getLastMessage().toString());
				} else if (input.matches("(?i)rename")) {
					context.print("From Name: ");
					String from = context.readLine();
					context.print("To Name: ");
					String to = context.readLine();
					client.rename(from, to);
					context.println(client.getLastMessage().toString());
				} else if (input.matches("(?i)dele .+")) {
					client.delete(input.substring(5));
					context.println(client.getLastMessage().toString());
				} else if (input.matches("(?i)mkd .+")) {
					client.makeDirectory(input.substring(4));
					context.println(client.getLastMessage().toString());
				} else if (input.matches("(?i)rmd .+")) {
					client.removeDirectory(input.substring(4));
					context.println(client.getLastMessage().toString());
				} else if (input.matches("(?i)actv")) {
					client.setTransferMode(TransferMode.Active);
					context.println("active mode");
				} else if (input.matches("(?i)pasv")) {
					client.setTransferMode(TransferMode.Passive);
					context.println("passive mode");
				} else if (input.startsWith("?"))
					context.println("syst, ascii, binary, pwd, cwd, cdup, list, mlsd, retr FILE, "
							+ "stor FILE, rename, dele FILE, mkd DIR, rmd DIR, actv, pasv, quit");
				else
					context.println("invalid command. type '?'");
			}
		} catch (IOException e) {
			logger.error("kraken-ftp: " + e.getMessage());
			context.println(e.getMessage());
		} catch (InterruptedException e) {
			context.println("interrupted");
		} finally {
			client.close();
		}
	}
}
