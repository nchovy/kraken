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

import org.krakenapps.honey.sshd.HoneyBaseCommandHandler;
import org.krakenapps.termio.TerminalOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ps extends HoneyBaseCommandHandler {
	private final Logger logger = LoggerFactory.getLogger(Ps.class.getName());

	@Override
	public int main(String[] args) {
		try {
			TerminalOutputStream out = getSession().getOutputStream();
			out.println("PID TTY          TIME CMD\r\n");
			out.println("24870 pts/1    00:00:00 su\r\n");
			out.println("24871 pts/1    00:00:00 bash\r\n");
			out.println("24908 pts/1    00:00:00 ps\r\n");
		} catch (Throwable t) {
			logger.error("kraken honey sshd: ps error", t);
		}
		return 0;
	}
}
