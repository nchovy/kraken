/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
