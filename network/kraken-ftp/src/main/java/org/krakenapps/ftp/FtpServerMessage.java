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
package org.krakenapps.ftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpServerMessage {
	private Logger logger = LoggerFactory.getLogger(FtpServerMessage.class.getName());
	private Integer code;
	private String[] messages;

	public FtpServerMessage(BufferedReader reader) {
		List<String> msgs = new ArrayList<String>();
		try {
			String resp = reader.readLine();
			if (resp.matches("\\d{3}( |-).*$")) {
				this.code = Integer.parseInt(resp.substring(0, 3));
				String msg = resp.substring(4);
				while (resp.charAt(3) == '-' || resp.charAt(0) == ' ') {
					resp = reader.readLine();
					if (resp.charAt(0) == ' ')
						msg += "\n" + resp;
					else if (resp.charAt(3) == '-') {
						msgs.add(msg);
						msg = resp.substring(4);
					}
				}
				msgs.add(msg);
				this.messages = msgs.toArray(new String[msgs.size()]);
			}
		} catch (IOException e) {
			logger.error("kraken-ftp: " + e.getMessage());
		}
	}

	public Integer getCode() {
		return code;
	}

	public boolean isCode(int code) {
		if (this.code == null)
			return false;
		return (this.code.intValue() == code);
	}

	public String[] getMessages() {
		return messages;
	}

	public String toString() {
		if (code == null)
			return "failed to receive message from server";

		String result = "";
		for (int i = 0; i < messages.length - 1; i++)
			result += code + "-" + messages[i] + "\n";
		result += code + " " + messages[messages.length - 1];

		return result;
	}
}
