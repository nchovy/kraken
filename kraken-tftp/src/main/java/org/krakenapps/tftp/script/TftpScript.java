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
package org.krakenapps.tftp.script;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.tftp.TftpArguments;
import org.krakenapps.tftp.TftpClient;
import org.krakenapps.tftp.TftpMode;
import org.krakenapps.tftp.TftpServer;

public class TftpScript implements Script {
	private ScriptContext context;

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "receive file from remote computer(run TFTP service)\n\ntftp.get [-i] host source [destination]", arguments = {
			@ScriptArgument(name = "[-i]", type = "string", description = "Specifies binary image transfer mode (also called octet). In binary image mode the file is moved literally, byte by byte. Use this mode when transferring binary files.", optional = true),
			@ScriptArgument(name = "host", type = "string", description = "Specifies the local or remote host."),
			@ScriptArgument(name = "source", type = "string", description = "Specifies the file to transfer."),
			@ScriptArgument(name = "destination", type = "string", description = "Specifies where to transfer the file.", optional = true) })
	public void get(String[] args) {
		try {
			TftpArguments arguments = parse(args);
			if (arguments == null)
				return;

			InetSocketAddress target = new InetSocketAddress(arguments.getHost(), 69);
			TftpClient client = new TftpClient();
			client.get(target, arguments.getMode(), arguments.getSource(), arguments.getDestination());

			context.println("transfer successful");
		} catch (FileNotFoundException e) {
			context.println("remote file not found");
		} catch (IOException e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "send your file to remote computer(run TFTP service)\n\ntftp.put [-i] host source [destination]", arguments = {
			@ScriptArgument(name = "[-i]", type = "string", description = "Specifies binary image transfer mode (also called octet). In binary image mode the file is moved literally, byte by byte. Use this mode when transferring binary files.", optional = true),
			@ScriptArgument(name = "host", type = "string", description = "Specifies the local or remote host."),
			@ScriptArgument(name = "source", type = "string", description = "Specifies the file to transfer."),
			@ScriptArgument(name = "destination", type = "string", description = "Specifies where to transfer the file.", optional = true) })
	public void put(String[] args) {
		try {
			TftpArguments arguments = parse(args);
			if (arguments == null)
				return;

			InetSocketAddress target = new InetSocketAddress(arguments.getHost(), 69);
			TftpClient client = new TftpClient();
			client.put(target, arguments.getMode(), arguments.getSource(), arguments.getDestination());

			context.println("transfer successful");
		} catch (FileNotFoundException e) {
			context.println("can't open local file");
		} catch (IOException e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "open your TFTP server(used port number 69)", arguments = { @ScriptArgument(name = "repository", type = "string", description = "Specifies repository of tftp server.") })
	public void open(String[] args) throws IOException {
		try {
			if (TftpScriptFactory.server == null) {
				TftpScriptFactory.server = new TftpServer(args[0]);
				TftpScriptFactory.server.start();
				context.println("tftp server opened");
			} else
				context.println("already opened");
		} catch (FileNotFoundException e) {
			context.println("invalid repository path");
		}
	}

	@ScriptUsage(description = "close your TFTP server")
	public void close(String[] args) {
		if (TftpScriptFactory.server != null) {
			TftpScriptFactory.server.stop();
			TftpScriptFactory.server = null;
			context.println("tftp server closed");
		} else {
			context.println("tftp server not opened");
		}
	}

	private TftpArguments parse(String[] args) {
		if (args.length == 2) {
			if (!evalHostName(args[0]))
				return null;
			return new TftpArguments(args[0], args[1]);
		}

		else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("-i")) {
				if (!evalHostName(args[1]))
					return null;

				return new TftpArguments(TftpMode.OCTET, args[1], args[2]);
			} else {
				if (!evalHostName(args[0]))
					return null;

				return new TftpArguments(args[0], args[1], args[2]);
			}
		}

		else if (args.length == 4) {
			if (args[0].equalsIgnoreCase("-i")) {
				if (evalHostName(args[1]))
					return new TftpArguments(TftpMode.OCTET, args[1], args[2], args[3]);
				else
					return null;
			} else {
				context.println("Invalid Argument Exception");
				return null;
			}
		} else {
			context.println("Invalid Argument Exception");
			return null;
		}

	}

	private boolean evalHostName(String host) {
		try {
			InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			return false;
		}
		return true;
	}
}