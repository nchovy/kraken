package org.krakenapps.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class CoreScript implements Script {
	private ScriptContext context;

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
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
			context.println("cp: cannot stat '" + from.getName() + "': No such file or directory");
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
				context.println("rm: cannot remove '" + token + "': No such file or directory");
			else
				f.delete();
		}
	}

	public void mv(String[] args) {
		File dir = (File) context.getSession().getProperty("dir");

		File from = new File(dir, args[0]);
		File to = new File(dir, args[1]);

		if (from.equals(to))
			context.println("mv: '" + from.getName() + "' and '" + to.getName() + "' are the same file");
		else if (!from.exists())
			context.println("mv: cannot stat '" + from.getName() + "': No such file or directory");
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

}
