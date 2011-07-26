package org.krakenapps.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;

import org.krakenapps.ansicode.ClearScreenCode;
import org.krakenapps.ansicode.MoveToCode;
import org.krakenapps.ansicode.SetColorCode;
import org.krakenapps.ansicode.ClearScreenCode.Option;
import org.krakenapps.ansicode.SetColorCode.Color;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.main.Kraken;
import org.krakenapps.pkg.HttpWagon;
import org.osgi.framework.BundleException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class CoreScript implements Script {
	private ScriptContext context;

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void cat(String[] args) throws IOException {
		File dir = (File) context.getSession().getProperty("dir");
		File f = new File(dir, args[0]);
		if (!f.exists()) {
			context.println("cat: " + f.getName()
					+ ": No such file or directory");
			return;
		}

		FileInputStream is = new FileInputStream(f);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;

			context.println(line);
		}
	}

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
			File f = new File(dir, url.getFile());
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

	public void pwd(String[] args) {
		File dir = (File) context.getSession().getProperty("dir");
		context.println(dir.getAbsolutePath());
	}

	public void cp(String[] args) throws IOException {
		File dir = (File) context.getSession().getProperty("dir");

		File from = new File(dir, args[0]);
		File to = new File(dir, args[1]);

		if (!from.exists())
			context.println("cp: cannot stat '" + from.getName()
					+ "': No such file or directory");
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

	public void rm(String[] args) {
		File dir = (File) context.getSession().getProperty("dir");

		for (String token : args) {
			File f = new File(dir, token);
			if (!f.exists())
				context.println("rm: cannot remove '" + token
						+ "': No such file or directory");
			else
				f.delete();
		}
	}

	public void mv(String[] args) {
		File dir = (File) context.getSession().getProperty("dir");

		File from = new File(dir, args[0]);
		File to = new File(dir, args[1]);

		if (from.equals(to))
			context.println("mv: '" + from.getName() + "' and '" + to.getName()
					+ "' are the same file");
		else if (!from.exists())
			context.println("mv: cannot stat '" + from.getName()
					+ "': No such file or directory");
		else
			from.renameTo(to);
	}

	public void ls(String[] args) {
		File dir = (File) context.getSession().getProperty("dir");
		for (File f : dir.listFiles()) {
			context.println(formatFileInfo(f));
		}
	}

	public void cd(String[] args) {
		File dir = (File) context.getSession().getProperty("dir");

		File newDir = null;
		if (args[0].equals("..")) {
			if (dir.getParentFile() != null)
				newDir = dir.getParentFile();
			else
				newDir = dir;
		} else
			newDir = new File(dir, args[0]);

		if (!(newDir.exists() && newDir.isDirectory()))
			context.println("No such file or directory");
		else
			context.getSession().setProperty("dir", newDir);
	}

	public void date(String[] args) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ssZ");
		context.println(dateFormat.format(new Date()));
	}

	public void ipconfig(String[] args) throws SocketException {
		Enumeration<NetworkInterface> it = NetworkInterface
				.getNetworkInterfaces();
		while (it.hasMoreElements()) {
			NetworkInterface nic = it.nextElement();
			byte[] b = nic.getHardwareAddress();
			String mac = "";
			if (b != null && b.length != 0)
				mac = String.format(" (%02X:%02X:%02X:%02X:%02X:%02X)", b[0],
						b[1], b[2], b[3], b[4], b[5]);

			context.printf("%s%s - %s\n", nic.getName(), mac,
					nic.getDisplayName());

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

	private String formatFileInfo(File f) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		String lastModified = dateFormat.format(new Date(f.lastModified()));
		String dir = f.isDirectory() ? "<DIR>" : "";
		char r = f.canRead() ? 'r' : '-';
		char w = f.canWrite() ? 'w' : '-';
		char x = f.canExecute() ? 'x' : '-';

		return String.format("%c%c%c %10d\t%s\t%s\t%s", r, w, x, f.length(),
				lastModified, dir, f.getName());
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

		context.print(new SetColorCode(Color.parse(b), Color.parse(f),
				highIntensity));
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

	public void scp(String[] args) throws JSchException, IOException {
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
				fos = new FileOutputStream(prefix == null ? lfile : prefix
						+ file);
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
