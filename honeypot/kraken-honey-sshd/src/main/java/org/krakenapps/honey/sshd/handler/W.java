package org.krakenapps.honey.sshd.handler;

import java.io.IOException;

import org.krakenapps.honey.sshd.HoneyBaseCommandHandler;
import org.krakenapps.termio.TerminalOutputStream;

public class W extends HoneyBaseCommandHandler {

	@Override
	public int main(String[] args) {
		try {
			TerminalOutputStream out = getSession().getOutputStream();
			out.write(" 00:09:32 up 12 days, 12:35,  1 user,  load average: 0.42, 0.47, 0.39\r\n".getBytes());
			out.write("USER     TTY      FROM              LOGIN@   IDLE   JCPU   PCPU WHAT\r\n".getBytes());
			out.write("xeraph   pts/0    112.153.134.55   00:09    0.00s  0.04s  0.00s w\r\n".getBytes());
		} catch (IOException e) {
		}

		return 0;
	}
}
