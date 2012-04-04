package org.krakenapps.honey.sshd.handler;

import org.krakenapps.ansicode.SetColorCode;
import org.krakenapps.ansicode.SetColorCode.Color;
import org.krakenapps.honey.sshd.HoneyBaseCommandHandler;
import org.krakenapps.termio.TerminalOutputStream;

public class Ls extends HoneyBaseCommandHandler {

	@Override
	public int main(String[] args) {
		TerminalOutputStream out = getSession().getOutputStream();
		String[] files = new String[] { "anaconda-ks.cfg", "install.log", "install.log.syslog", "install.sh" };

		try {
			for (String file : files) {
				if (file.endsWith(".sh")) {
					out.write(new SetColorCode(Color.Black, Color.Green).toByteArray());
				} else {
					out.write(new SetColorCode(Color.Black, Color.White).toByteArray());
				}

				out.print(file + "  ");
			}

			out.write(new SetColorCode(Color.Black, Color.Blue).toByteArray());
			out.print("log");

			out.println("");
			out.write(new SetColorCode(Color.Reset, Color.Reset).toByteArray());
		} catch (Throwable t) {
		}

		return 0;
	}

}
