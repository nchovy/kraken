package org.krakenapps.honey.sshd.handler;

import org.krakenapps.honey.sshd.HoneyBaseCommandHandler;
import org.krakenapps.termio.TerminalOutputStream;

public class Uname extends HoneyBaseCommandHandler {

	@Override
	public int main(String[] args) {
		try {
			TerminalOutputStream out = getSession().getOutputStream();
			out.write("Linux yankees 2.6.18-274.7.1.el5xen #1 SMP Thu Oct 20 17:06:34 EDT 2011 x86_64 x86_64 x86_64 GNU/Linux\r\n"
					.getBytes());
		} catch (Throwable t) {
		}
		return 0;
	}
}
