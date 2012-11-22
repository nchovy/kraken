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
package org.krakenapps.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.krakenapps.ansicode.ClearScreenCode;
import org.krakenapps.ansicode.ClearScreenCode.Option;
import org.krakenapps.ansicode.MoveToCode;
import org.krakenapps.ansicode.SetColorCode;
import org.krakenapps.ansicode.SetColorCode.Color;
import org.krakenapps.api.DirectoryAutoCompleter;
import org.krakenapps.api.PathAutoCompleter;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.main.Kraken;
import org.krakenapps.pkg.HttpWagon;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class CoreScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(CoreScript.class.getName());
	private ScriptContext context;

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "tcp scan", arguments = {
			@ScriptArgument(name = "ip", type = "string", description = "ip address or host name"),
			@ScriptArgument(name = "port", type = "integer", description = "port number"),
			@ScriptArgument(name = "timeout", type = "integer", description = "timeout(ms)", optional = true) })
	public void tcpscan(String[] args) {
		InetSocketAddress endpoint = null;
		Socket socket = new Socket();
		try {
			InetAddress host = InetAddress.getByName(args[0]);
			Integer port = Integer.parseInt(args[1]);
			Integer timeout = (args.length > 2) ? Integer.parseInt(args[2]) : 3000;

			endpoint = new InetSocketAddress(host, port);

			context.println("trying to connect " + endpoint);
			socket.connect(endpoint, timeout);
			context.println("opened");
		} catch (UnknownHostException e) {
			context.println("invalid host [" + args[0] + "]");
		} catch (SocketTimeoutException e) {
			context.println("timeout");
		} catch (IOException e) {
			context.println("not opened: " + e.getMessage());
			logger.error("kraken core: cannot connect to " + endpoint, e);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}

	}

	@ScriptUsage(description = "print file content", arguments = { @ScriptArgument(name = "file path", type = "string", description = "file path", autocompletion = PathAutoCompleter.class) })
	public void cat(String[] args) throws IOException {
		File dir = (File) context.getSession().getProperty("dir");
		File f = canonicalize(dir, args[0]);
		if (f == null || !f.exists()) {
			context.println("cat: " + f.getName() + ": No such file or directory");
			return;
		}

		FileInputStream is = new FileInputStream(f);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;

				context.println(line);
			}
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
	}

	@ScriptUsage(description = "edit file content", arguments = { @ScriptArgument(name = "file path", type = "string", description = "file path", autocompletion = PathAutoCompleter.class) })
	public void edit(String[] args) {
		try {
			File dir = (File) context.getSession().getProperty("dir");
			File f = new File(dir, args[0]);
			new Editor(context).open(f);
		} catch (IOException e) {
			context.println(e.getMessage());
		}
	}

	public void wget(String[] args) throws MalformedURLException {
		File dir = (File) context.getSession().getProperty("dir");

		URL url = new URL(args[0]);
		FileOutputStream os = null;
		try {
			byte[] b = HttpWagon.download(url);
			String fileStr = url.getFile();
			if (fileStr.indexOf('/') != -1)
				fileStr = fileStr.substring(fileStr.lastIndexOf('/') + 1);
			File f = new File(dir, fileStr);
			os = new FileOutputStream(f);
			os.write(b);

			context.println("downloaded " + f.getAbsolutePath());
		} catch (Exception e) {
			context.println(e.getMessage());
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
				}
		}
	}

	public void pwd(String[] args) throws IOException {
		File dir = (File) context.getSession().getProperty("dir");
		context.println(dir.getCanonicalPath());
	}

	@ScriptUsage(description = "copy file", arguments = {
			@ScriptArgument(name = "source file path", type = "string", description = "source file path", autocompletion = PathAutoCompleter.class),
			@ScriptArgument(name = "destination file path", type = "string", description = "destination file path", autocompletion = PathAutoCompleter.class) })
	public void cp(String[] args) throws IOException {
		File dir = (File) context.getSession().getProperty("dir");

		File from = canonicalize(dir, args[0]);
		File to = canonicalize(dir, args[1]);

		if (from == null || !from.exists())
			context.println("cp: cannot stat '" + args[0] + "': No such file or directory");
		else if (to == null)
			context.println("cp: cannot stat '" + args[1] + "': No such file or directory");
		else {
			FileChannel inChannel = new FileInputStream(from).getChannel();
			FileChannel outChannel = new FileOutputStream(to).getChannel();

			try {
				inChannel.transferTo(0, inChannel.size(), outChannel);
			} catch (IOException e) {
				context.println(e.getMessage());
			} finally {
				inChannel.close();
				outChannel.close();
			}
		}
	}

	@ScriptUsage(description = "delete file", arguments = { @ScriptArgument(name = "file path", type = "string", description = "file path", autocompletion = PathAutoCompleter.class) })
	public void rm(String[] args) throws IOException {
		File dir = (File) context.getSession().getProperty("dir");

		for (String token : args) {
			File f = canonicalize(dir, token);
			if (f == null || !f.exists())
				context.println("rm: cannot remove '" + token + "': No such file or directory");
			else {
				boolean ret = f.delete();
				if (!ret)
					context.println("rm: permission denied");
			}
		}
	}

	@ScriptUsage(description = "move file", arguments = {
			@ScriptArgument(name = "source file path", type = "string", description = "source file path", autocompletion = PathAutoCompleter.class),
			@ScriptArgument(name = "destination file path", type = "string", description = "destination file path", autocompletion = PathAutoCompleter.class) })
	public void mv(String[] args) throws IOException {
		File dir = (File) context.getSession().getProperty("dir");

		File from = canonicalize(dir, args[0]);
		File to = canonicalize(dir, args[1]);

		if (from == null)
			context.println("mv: cannot stat '" + args[0] + "': No such file or directory");
		else if (to == null)
			context.println("mv: cannot stat '" + args[1] + "': No such file or directory");
		else if (from.equals(to))
			context.println("mv: '" + from.getName() + "' and '" + to.getName() + "' are the same file");
		else if (!from.exists())
			context.println("mv: cannot stat '" + from.getName() + "': No such file or directory");
		else {
			boolean ret = from.renameTo(to);
			if (!ret)
				context.println("mv: cannot rename '" + from.getName() + "' to '" + to.getName() + "'");
		}
	}

	@ScriptUsage(description = "list files", arguments = { @ScriptArgument(name = "path", type = "string", description = "path", optional = true, autocompletion = PathAutoCompleter.class) })
	public void ls(String[] args) throws IOException {
		File dir = (File) context.getSession().getProperty("dir");
		List<File> targets = new LinkedList<File>();
		if (args.length == 0)
			targets.add(dir);

		for (String arg : args) {
			File target = canonicalize(dir, arg);
			if (target != null)
				targets.add(target);
		}

		for (File target : targets) {
			if (!target.exists()) {
				context.println("ls: cannot access " + target.getName() + " :No such file or directory");
				return;
			}

			if (targets.size() > 1)
				context.println(target.getName() + ":");

			File[] list = target.listFiles();
			if (list == null)
				continue;

			for (File f : list) {
				context.println(formatFileInfo(f));
			}
		}
	}

	@ScriptUsage(description = "change directory", arguments = { @ScriptArgument(name = "path", type = "string", description = "absolute or relative directory path", autocompletion = DirectoryAutoCompleter.class) })
	public void cd(String[] args) throws IOException {
		if (args.length == 0)
			return;

		File dir = (File) context.getSession().getProperty("dir");

		File newDir = canonicalize(dir, args[0]);

		if (newDir == null || !(newDir.exists() && newDir.isDirectory()))
			context.println("No such file or directory");
		else
			context.getSession().setProperty("dir", newDir);
	}

	private File canonicalize(File dir, String path) throws IOException {
		if (path.startsWith("/"))
			return new File(path).getCanonicalFile();
		else
			return new File(dir, path).getCanonicalFile();
	}

	public void date(String[] args) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		context.println(dateFormat.format(new Date()));
	}

	public void ipconfig(String[] args) throws SocketException {
		Enumeration<NetworkInterface> it = NetworkInterface.getNetworkInterfaces();
		while (it.hasMoreElements()) {
			NetworkInterface nic = it.nextElement();
			byte[] b = nic.getHardwareAddress();
			String mac = "";
			if (b != null && b.length != 0)
				mac = String.format(" (%02X:%02X:%02X:%02X:%02X:%02X)", b[0], b[1], b[2], b[3], b[4], b[5]);

			context.printf("%s%s - %s\n", nic.getName(), mac, nic.getDisplayName());

			Enumeration<InetAddress> in = nic.getInetAddresses();
			while (in.hasMoreElements()) {
				InetAddress addr = in.nextElement();
				context.println("  " + addr.getHostAddress());
			}
		}
	}

	@ScriptUsage(description = "set foreground and background colors", arguments = {})
	public void color(String[] args) {
		if (args.length == 0) {
			changeColor("07");
			return;
		}

		if (args.length < 2)
			return;

		String code = args[0];
		if (code.length() != 2)
			return;

		changeColor(code);
	}

	public void guid(String[] args) {
		context.println(UUID.randomUUID().toString());
	}

	public void shutdown(String[] args) {
		if (Kraken.isServiceMode()) {
			context.println("You cannot use shutdown command when Kraken is running in service mode.");
		}
		try {
			Kraken.getContext().getBundle(0).stop();
		} catch (BundleException e) {
		}
	}

	public void clear(String[] args) {
		context.print(new MoveToCode(1, 1));
		context.print(new ClearScreenCode(Option.EntireScreen));
	}

	public void set(String[] args) {
		if (args.length == 0) {
			Properties props = System.getProperties();
			List<String> keys = new ArrayList<String>(props.stringPropertyNames());
			Collections.sort(keys);
			for (String key : keys) {
				context.printf("%s=%s\n", key, props.getProperty(key));
			}
		} else if (args.length == 1) {
			if (!args[0].contains("=")) {
				context.printf("%s=%s\n", args[0], System.getProperty(args[0]));
			} else {
				String[] kv = args[0].split("=", 2);
				System.setProperty(kv[0].trim(), kv[1].trim());
				context.println("set");
			}
		} else {
			for (String arg : args) {
				context.printf("%s=%s\n", arg, System.getProperty(arg));
			}
		}
	}

	private String formatFileInfo(File f) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String lastModified = dateFormat.format(new Date(f.lastModified()));
		String dir = f.isDirectory() ? "<DIR>" : "";
		char r = f.canRead() ? 'r' : '-';
		char w = f.canWrite() ? 'w' : '-';
		char x = f.canExecute() ? 'x' : '-';

		return String.format("%c%c%c %10d\t%s\t%s\t%s", r, w, x, f.length(), lastModified, dir, f.getName());
	}

	private void changeColor(String code) {
		code = code.toUpperCase();
		char c1 = code.charAt(0);
		char c2 = code.charAt(1);

		boolean highIntensity = false;
		if (c2 >= '9')
			highIntensity = true;

		byte b = invert(c1);
		byte f = invert(c2);

		context.print(new SetColorCode(Color.parse(b), Color.parse(f), highIntensity));
		context.print(new MoveToCode(1, 1));
		context.print(new ClearScreenCode(Option.EntireScreen));
	}

	private byte invert(char c) {
		final byte[] code = new byte[] { 0, 4, 2, 6, 1, 5, 3, 7, 7 };
		byte b = (byte) ((c >= 'A') ? (c - 'A' + 2) : (c - '0'));
		if (c == '9')
			b = 1;

		b = (byte) (b % code.length);
		return code[b];
	}

	@ScriptUsage(description = "scp", arguments = {
			@ScriptArgument(name = "file from", type = "string", description = "local path or user@host:path format"),
			@ScriptArgument(name = "file to", type = "string", description = "local path or user@host:path format") })
	public void scp(String[] args) throws JSchException, IOException {
		if (args[0].contains("@"))
			scpFrom(args);
		else
			scpTo(args);
	}

	private void scpFrom(String[] args) {
		// scp from
		FileOutputStream fos = null;
		try {
			String user = args[0].substring(0, args[0].indexOf('@'));
			args[0] = args[0].substring(args[0].indexOf('@') + 1);
			String host = args[0].substring(0, args[0].indexOf(':'));
			String rfile = args[0].substring(args[0].indexOf(':') + 1);
			String lfile = args[1];

			String prefix = null;
			if (new File(lfile).isDirectory()) {
				prefix = lfile + File.separator;
			}

			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, 22);
			session.setUserInfo(new MyUserInfo());
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();

			String command = "scp -f " + rfile;
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			byte[] buf = new byte[1024];

			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();

			while (true) {
				int c = checkAck(in);
				if (c != 'C') {
					break;
				}

				// read '0644 '
				in.read(buf, 0, 5);

				long filesize = 0L;
				while (true) {
					if (in.read(buf, 0, 1) < 0) {
						// error
						break;
					}
					if (buf[0] == ' ')
						break;
					filesize = filesize * 10L + (long) (buf[0] - '0');
				}

				String file = null;
				for (int i = 0;; i++) {
					in.read(buf, i, 1);
					if (buf[i] == (byte) 0x0a) {
						file = new String(buf, 0, i);
						break;
					}
				}

				// send '\0'
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();

				// read a content of lfile
				fos = new FileOutputStream(prefix == null ? lfile : prefix + file);
				int foo;
				while (true) {
					if (buf.length < filesize)
						foo = buf.length;
					else
						foo = (int) filesize;
					foo = in.read(buf, 0, foo);
					if (foo < 0) {
						// error
						break;
					}
					fos.write(buf, 0, foo);
					filesize -= foo;
					if (filesize == 0L)
						break;
				}
				fos.close();
				fos = null;

				if (checkAck(in) != 0) {
					throw new IllegalStateException();
				}

				// send '\0'
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();
			}

			session.disconnect();

		} catch (Exception e) {
			context.println(e.getMessage());
			try {
				if (fos != null)
					fos.close();
			} catch (Exception ee) {
			}
		}
	}

	private void scpTo(String[] args) {
		FileInputStream fis = null;
		try {
			String lfile = args[0];
			String user = args[1].substring(0, args[1].indexOf('@'));
			args[1] = args[1].substring(args[1].indexOf('@') + 1);
			String host = args[1].substring(0, args[1].indexOf(':'));
			String rfile = args[1].substring(args[1].indexOf(':') + 1);

			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, 22);

			// username and password will be given via UserInfo interface.
			UserInfo ui = new MyUserInfo();
			session.setUserInfo(ui);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();

			// exec 'scp -t rfile' remotely
			String command = "scp -p -t " + rfile;
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			if (checkAck(in) != 0) {
				System.exit(0);
			}

			// send "C0644 filesize filename", where filename should not include
			// '/'
			long filesize = (new File(lfile)).length();
			command = "C0644 " + filesize + " ";
			if (lfile.lastIndexOf('/') > 0) {
				command += lfile.substring(lfile.lastIndexOf('/') + 1);
			} else {
				command += lfile;
			}
			command += "\n";
			out.write(command.getBytes());
			out.flush();
			if (checkAck(in) != 0) {
				System.exit(0);
			}

			// send a content of lfile
			fis = new FileInputStream(lfile);
			byte[] buf = new byte[1024];
			while (true) {
				int len = fis.read(buf, 0, buf.length);
				if (len <= 0)
					break;
				out.write(buf, 0, len); // out.flush();
			}
			fis.close();
			fis = null;
			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
			if (checkAck(in) != 0) {
				System.exit(0);
			}
			out.close();

			channel.disconnect();
			session.disconnect();
		} catch (Exception e) {
			System.out.println(e);
			try {
				if (fis != null)
					fis.close();
			} catch (Exception ee) {
			}
		}
	}

	int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				context.println(sb.toString());
			}
			if (b == 2) { // fatal error
				context.println(sb.toString());
			}
		}
		return b;
	}

	private class MyUserInfo implements UserInfo {
		private String passphrase;
		private String password;

		@Override
		public String getPassphrase() {
			return passphrase;
		}

		@Override
		public String getPassword() {
			return password;
		}

		@Override
		public boolean promptPassphrase(String message) {
			context.print(message + ": ");
			try {
				passphrase = context.readPassword();
			} catch (InterruptedException e) {
				return false;
			}
			return true;
		}

		@Override
		public boolean promptPassword(String message) {
			context.print(message + ": ");
			try {
				password = context.readPassword();
			} catch (InterruptedException e) {
				return false;
			}

			return true;
		}

		@Override
		public boolean promptYesNo(String message) {
			return false;
		}

		@Override
		public void showMessage(String message) {
			context.println(message);
		}
	}
}
