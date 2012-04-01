package org.krakenapps.honey.sshd.handler;

import java.io.IOException;

import org.krakenapps.ansicode.ClearScreenCode;
import org.krakenapps.ansicode.MoveToCode;
import org.krakenapps.ansicode.ClearScreenCode.Option;
import org.krakenapps.honey.sshd.HoneyBaseCommandHandler;

public class Clear extends HoneyBaseCommandHandler {

	@Override
	public int main(String[] args) {
		try {
			byte[] b = new MoveToCode(1, 1).toByteArray();
			getSession().getOutputStream().write(b);

			b = new ClearScreenCode(Option.EntireScreen).toByteArray();
			getSession().getOutputStream().write(b);
		} catch (IOException e) {
		}

		return 0;
	}
}
