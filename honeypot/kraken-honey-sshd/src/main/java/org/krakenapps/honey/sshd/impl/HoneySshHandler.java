package org.krakenapps.honey.sshd.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;

import org.krakenapps.honey.sshd.HoneySshService;
import org.krakenapps.termio.TerminalEventListener;
import org.krakenapps.termio.TerminalSession;

public class HoneySshHandler implements TerminalEventListener {
	private HoneySshService sshd;
	private TerminalSession session;

	public HoneySshHandler(HoneySshService sshd, TerminalSession session) {
		this.sshd = sshd;
		this.session = session;
	}

	@Override
	public void onConnected(TerminalSession session) {
		print("Last login: Sat Mar 31 17:55:39 2012 from 14.56.93.242\r\n");
		printPrompt();
	}

	@Override
	public void onCommand(TerminalSession session, int command, byte[] option) {
	}

	@Override
	public void onData(TerminalSession session, int b) {
		try {
			if (b == '\n' || b == '\r') {
				print("\r\n");
				printPrompt();
			} else {
				session.write(b);
				session.flush();
			}
		} catch (IOException e) {
		}
	}

	private void printPrompt() {
		String prompt = "[" + session.getUsername() + "@" + sshd.getHostname() + " ~]";
		if (session.getUsername().equals("root"))
			prompt += "# ";
		else
			prompt += "$ ";
		print(prompt);
	}

	private void print(String s) {
		try {
			session.write(s.getBytes("utf-8"));
			session.flush();
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
		}
	}
}
