package org.krakenapps.console;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.krakenapps.ansicode.ClearScreenCode;
import org.krakenapps.ansicode.MoveToCode;
import org.krakenapps.ansicode.SetColorCode;
import org.krakenapps.ansicode.ClearScreenCode.Option;
import org.krakenapps.ansicode.SetColorCode.Color;
import org.krakenapps.api.AccountManager;
import org.krakenapps.api.FunctionKeyEvent;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptOutputStream;
import org.krakenapps.api.ScriptSession;
import org.krakenapps.api.FunctionKeyEvent.KeyCode;
import org.krakenapps.main.Kraken;
import org.krakenapps.pkg.HttpWagon;
import org.krakenapps.script.ScriptContextImpl;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShellSession {
	public static final String KRAKEN_PROMPT = "kraken> ";
	private static final String hostname;

	final Logger logger = LoggerFactory.getLogger(ShellSession.class.getName());

	private Map<String, Object> attributes;
	private ScriptContextImpl sc;
	private QuitHandler quit = null;
	private String lastChar = null;

	static {
		String h = "unknown";
		try {
			h = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		}

		hostname = h;
	}

	public ShellSession(ScriptContextImpl scriptContext) {
		this.attributes = new HashMap<String, Object>();
		this.sc = scriptContext;
	}

	public void setQuitHandler(QuitHandler quit) {
		this.quit = quit;
	}

	public ScriptContext getScriptContext() {
		return sc;
	}

	public void printBanner() {
		sc.getOutputStream().println(Kraken.BANNER);
	}

	public void handleMessage(Object message) throws InterruptedException, IOException {
		if (ignoreLF(message))
			return;

		if (sc.getCurrentScript() == null)
			processShell(message);
		else
			supplyInputToScript(message);
	}

	// normalize CRLF
	private boolean ignoreLF(Object message) {
		if (!(message instanceof String))
			return false;

		String c = (String) message;
		if (lastChar != null && lastChar.equals("\r") && c.equals("\n")) {
			lastChar = c;
			return true;
		}

		lastChar = c;
		return false;
	}

	private void supplyInputToScript(Object message) {
		if (message instanceof String) {
			String character = (String) message;
			sc.transferInput(character.charAt(0));
		} else if (message instanceof FunctionKeyEvent) {
			FunctionKeyEvent keyEvent = (FunctionKeyEvent) message;
			sc.transferInput(keyEvent);
		} else {
			throw new AssertionError("not supported.");
		}
	}

	private void processShell(Object message) throws InterruptedException, IOException {
		ConsoleController controller = sc.getController();
		if (message instanceof FunctionKeyEvent) {
			FunctionKeyEvent ev = (FunctionKeyEvent) message;

			// suppress function key while logon
			if (attributes.get("principal") == null && !ev.isPressed(KeyCode.BACKSPACE))
				return;

			controller.onFunctionKeyPressed(ev);
			return;
		}

		ScriptOutputStream out = sc.getOutputStream();
		controller.onCharacterInput((String) message);

		if (controller.hasLine() == false)
			return;

		String line = controller.getLine();

		if (attributes.get("principal") == null) {
			String input = line.replaceAll("\r\n", "");
			ScriptSession session = sc.getSession();

			if (session.getProperty("kraken.user") == null) {
				session.setProperty("kraken.user", input);
				out.print("password: ");
				sc.turnEchoOff();
				return;
			} else {
				String name = (String) session.getProperty("kraken.user");

				AccountManager accountManager = sc.getAccountManager();
				if (accountManager.verifyPassword(name, input)) {
					setPrincipal(name);
					sc.turnEchoOn();
					out.print("\r\n");

					printBanner();
					printPrompt();
					return;
				} else {
					out.print("\r\nAccess denied\r\n");
					Thread.sleep(2000);
					out.print("\r\npassword: ");
					return;
				}
			}
		}

		if (line.trim().length() == 0) {
			printPrompt();
			return;
		}

		if (handleEmbeddedCommands(out, line))
			return;

		sc.getHistoryManager().pushLine(line);

		if (logger.isDebugEnabled())
			logger.debug("message received: [" + line + "]");

		try {
			runScript(line);
			return;
		} catch (InstantiationException e) {
			out.print("Script instantiation failed.\r\n");
		} catch (IllegalAccessException e) {
			out.print("Script constructor must have a public access.\r\n");
		} catch (NullPointerException e) {
			out.print("syntax error.\r\n");
		}

		printPrompt();
	}

	public void setPrincipal(String name) {
		attributes.put("principal", name);
	}

	private byte invert(char c) {
		final byte[] code = new byte[] { 0, 4, 2, 6, 1, 5, 3, 7, 7 };
		byte b = (byte) ((c >= 'A') ? (c - 'A' + 2) : (c - '0'));
		if (c == '9')
			b = 1;

		b = (byte) (b % code.length);
		return code[b];
	}

	private boolean handleEmbeddedCommands(ScriptOutputStream out, String line) throws IOException {
		line = line.trim();

		int space = line.indexOf(' ');
		if (space < 0)
			space = line.length();

		String command = line.substring(0, space).trim();

		if (command.equals("wget")) {
			File dir = (File) sc.getSession().getProperty("dir");
			String[] tokens = line.split(" ");

			URL url = new URL(tokens[1]);
			FileOutputStream os = null;
			try {
				byte[] b = HttpWagon.download(url);
				File f = new File(dir, url.getFile());
				os = new FileOutputStream(f);
				os.write(b);
				out.println("downloaded " + f.getAbsolutePath());
			} catch (Exception e) {
				out.println(e.getMessage());
			} finally {
				if (os != null)
					os.close();
			}

			printPrompt();
			return true;
		}

		if (command.equals("pwd")) {
			File dir = (File) sc.getSession().getProperty("dir");
			out.println(dir.getAbsolutePath());
			printPrompt();
			return true;
		}

		if (command.equals("cp")) {
			File dir = (File) sc.getSession().getProperty("dir");
			String[] tokens = line.split(" ");

			File from = new File(dir, tokens[1]);
			File to = new File(dir, tokens[2]);

			if (!from.exists())
				out.println("cp: cannot stat '" + from.getName() + "': No such file or directory");
			else {
				FileChannel inChannel = new FileInputStream(from).getChannel();
				FileChannel outChannel = new FileOutputStream(to).getChannel();

				try {
					inChannel.transferTo(0, inChannel.size(), outChannel);
				} catch (IOException e) {
					out.println(e.getMessage());
				} finally {
					inChannel.close();
					outChannel.close();
				}
			}

			printPrompt();
			return true;
		}

		if (command.equals("rm")) {
			File dir = (File) sc.getSession().getProperty("dir");
			String[] tokens = line.split(" ");

			int i = 0;
			for (String token : tokens) {
				if (i++ == 0)
					continue;

				File f = new File(dir, token);
				if (!f.exists())
					out.println("rm: cannot remove '" + token + "': No such file or directory");
				else
					f.delete();
			}

			printPrompt();
			return true;
		}

		if (command.equals("mv")) {
			File dir = (File) sc.getSession().getProperty("dir");
			String[] tokens = line.split(" ");

			File from = new File(dir, tokens[1]);
			File to = new File(dir, tokens[2]);

			if (from.equals(to))
				out.println("mv: '" + from.getName() + "' and '" + to.getName() + "' are the same file");
			else if (!from.exists())
				out.println("mv: cannot stat '" + from.getName() + "': No such file or directory");
			else
				from.renameTo(to);
			
			printPrompt();
			return true;
		}

		if (command.equals("ls")) {
			File dir = (File) sc.getSession().getProperty("dir");
			for (File f : dir.listFiles()) {
				out.println(formatFileInfo(f));
			}

			printPrompt();
			return true;
		}

		if (command.equals("cd")) {
			String[] tokens = line.split(" ");
			File dir = (File) sc.getSession().getProperty("dir");

			File newDir = null;
			if (tokens[1].equals("..")) {
				if (dir.getParentFile() != null)
					newDir = dir.getParentFile();
				else
					newDir = dir;
			} else
				newDir = new File(dir, tokens[1]);

			if (!(newDir.exists() && newDir.isDirectory()))
				out.println("No such file or directory");
			else
				sc.getSession().setProperty("dir", newDir);

			printPrompt();
			return true;
		}

		if (line.equals("date")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
			out.println(dateFormat.format(new Date()));
			printPrompt();
			return true;
		}

		if (line.equals("ipconfig")) {
			Enumeration<NetworkInterface> it = NetworkInterface.getNetworkInterfaces();
			while (it.hasMoreElements()) {
				NetworkInterface nic = it.nextElement();
				byte[] b = nic.getHardwareAddress();
				String mac = "";
				if (b != null && b.length != 0)
					mac = String.format(" (%02X:%02X:%02X:%02X:%02X:%02X)", b[0], b[1], b[2], b[3], b[4], b[5]);

				out.printf("%s%s - %s\n", nic.getName(), mac, nic.getDisplayName());

				Enumeration<InetAddress> in = nic.getInetAddresses();
				while (in.hasMoreElements()) {
					InetAddress addr = in.nextElement();
					out.println("  " + addr.getHostAddress());
				}
			}

			printPrompt();
			return true;
		}

		if (line.startsWith("color")) {
			if (line.equals("color")) {
				changeColor(out, "07");
				return true;
			}

			String[] tokens = line.split(" ");
			if (tokens.length < 2)
				return false;

			String code = tokens[1];
			if (code.length() != 2)
				return false;

			changeColor(out, code);
			return true;
		}

		if (line.equals("guid")) {
			out.println(UUID.randomUUID().toString());
			printPrompt();
			return true;
		}

		if (line.equals("quit") || line.equals("exit")) {
			if (quit != null) {
				quit.onQuit();
				quit = null;
			} else {
				throw new IOException("quit");
			}
		}

		if (line.equals("shutdown")) {
			if (Kraken.isServiceMode()) {
				sc.getOutputStream().println("You cannot use shutdown command when Kraken is running in service mode.");
				printPrompt();
				return true;
			}
			try {
				Kraken.getContext().getBundle(0).stop();
				return true;
			} catch (BundleException e) {
				e.printStackTrace();
			}
		}

		if (line.equals("clear")) {
			out.print(new MoveToCode(1, 1));
			out.print(new ClearScreenCode(Option.EntireScreen));
			printPrompt();
			return true;
		}

		// putty send only CR at ssh mode when you hit enter.
		if (line.equals("\r") || line.equals("\r\n")) {
			printPrompt();
			return true;
		}

		return false;
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

	private void changeColor(ScriptOutputStream out, String code) {
		code = code.toUpperCase();
		char c1 = code.charAt(0);
		char c2 = code.charAt(1);

		boolean highIntensity = false;
		if (c2 >= '9')
			highIntensity = true;

		byte b = invert(c1);
		byte f = invert(c2);

		out.print(new SetColorCode(Color.parse(b), Color.parse(f), highIntensity));
		out.print(new MoveToCode(1, 1));
		out.print(new ClearScreenCode(Option.EntireScreen));
		printPrompt();
	}

	public static String getPrompt() {
		return "kraken@" + hostname + "> ";
	}

	public void printPrompt() {
		sc.getOutputStream().print(getPrompt());
	}

	private void runScript(String line) throws InstantiationException, IllegalAccessException {
		Thread t = new Thread(new ScriptRunner(sc, line), "Kraken Script Runner");
		t.start();
	}
}
