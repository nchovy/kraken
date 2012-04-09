package org.krakenapps.honey.sshd.handler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.krakenapps.honey.sshd.HoneyBaseCommandHandler;
import org.krakenapps.honey.sshd.HoneyCommandHandler;
import org.krakenapps.honey.sshd.HoneySshService;
import org.krakenapps.honey.sshd.HoneySshSession;
import org.krakenapps.termio.TerminalInputStream;
import org.krakenapps.termio.TerminalOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shell extends HoneyBaseCommandHandler implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(Shell.class.getName());

	private Thread t;

	// line buffering
	private byte[] buf;

	// cursor position
	private int pos;

	// current line buffer length
	private int len;

	public Shell() {
		this.buf = new byte[8096];
	}

	public void start() {
		t = new Thread(this);
		t.start();
	}

	public void kill() {

	}

	private void execute(String line) throws IOException {
		String[] tokens = line.split("[\t ]");
		String command = tokens[0];

		HoneyCommandHandler handler = null;
		if (command.equals("w")) {
			handler = new W();
		} else if (command.startsWith("uname")) {
			handler = new Uname();
		} else if (command.equals("clear")) {
			handler = new Clear();
		} else if (command.equals("ls")) {
			handler = new Ls();
		} else if (command.equals("ps")) {
			handler = new Ps();
		} else if (command.equals("pwd")) {
			handler = new Pwd();
		} else if (command.equals("id")) {
			handler = new Id();
		} else if (command.equals("adduser")) {
			handler = new Adduser();
		} else if (command.equals("pwd")) {
			handler = new Pwd();
		} else if (command.equals("cd")) {
			handler = new Cd();
		}

		if (handler != null) {
			handler.setSession(getSession());
			handler.main(tokens);
		} else {
			String error = "-bash: " + command + ": command not found\r\n";
			getSession().getOutputStream().write(error.getBytes());
		}
	}

	private void printPrompt() {
		HoneySshSession session = getSession();
		HoneySshService sshd = session.getHoneySshService();
		String prompt = "[" + session.getUsername() + "@" + sshd.getHostname() + " ~]";
		if (session.getUsername().equals("root"))
			prompt += "# ";
		else
			prompt += "$ ";
		print(prompt);
	}

	private void print(String s) {
		try {
			TerminalOutputStream out = getSession().getOutputStream();
			out.write(s.getBytes("utf-8"));
			out.flush();
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
		}
	}

	@Override
	public int main(String[] args) {
		getSession().setEnvironmentVariable("$PWD", "/");

		print("Last login: Sat Mar 31 17:55:39 2012 from 14.56.93.242\r\n");
		printPrompt();

		try {
			TerminalInputStream in = getSession().getInputStream();
			while (true) {
				int b = in.read();
				if (b == '\n' || b == '\r') {
					String line = new String(buf, 0, len);
					print("\r\n");

					if (!line.trim().isEmpty())
						execute(line);

					printPrompt();
					len = 0;
				} else {
					buf[len++] = (byte) b;

					TerminalOutputStream out = getSession().getOutputStream();
					out.write(b);
					out.flush();
				}
			}
		} catch (IOException e) {

		}

		return 0;
	}

	@Override
	public void run() {
		main(new String[] {});
	}

}
