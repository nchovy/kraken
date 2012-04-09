package org.krakenapps.honey.sshd.handler;

import org.krakenapps.honey.sshd.HoneyBaseCommandHandler;

public class Cd extends HoneyBaseCommandHandler {

	@Override
	public int main(String[] args) {
		getSession().setEnvironmentVariable("$PWD", args[1]);
		return 0;
	}

}
