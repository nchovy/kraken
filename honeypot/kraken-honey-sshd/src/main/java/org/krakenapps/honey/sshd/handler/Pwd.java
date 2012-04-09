package org.krakenapps.honey.sshd.handler;

import java.io.IOException;

import org.krakenapps.honey.sshd.HoneyBaseCommandHandler;

public class Pwd extends HoneyBaseCommandHandler {

	@Override
	public int main(String[] args) {
		String pwd = getSession().getEnvironmentVariable("$PWD");
		try {
			getSession().getOutputStream().println(pwd);
		} catch (IOException e) {
		}
		return 0;
	}

}
