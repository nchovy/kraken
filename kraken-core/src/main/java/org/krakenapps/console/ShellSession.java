
package org.krakenapps.console;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
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
import org.krakenapps.script.ScriptContextImpl;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShellSession {
	public static final String KRAKEN_PROMPT = "kraken> ";
	final Logger logger = LoggerFactory.getLogger(ShellSession.class.getName());

	private Map<String, Object> attributes;
	private ScriptContextImpl sc;
	private QuitHandler quit = null;
	private String lastChar = null;

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

	public void printPrompt() {
		sc.getOutputStream().print(KRAKEN_PROMPT);
	}

	private void runScript(String line) throws InstantiationException, IllegalAccessException {
		Thread t = new Thread(new ScriptRunner(sc, line), "Kraken Script Runner");
		t.start();
	}
}
