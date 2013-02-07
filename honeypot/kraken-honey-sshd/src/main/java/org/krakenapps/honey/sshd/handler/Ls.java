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
