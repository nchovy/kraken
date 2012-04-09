/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.logparser.syslog.juniper.attack;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogVariableExtracter {
	private static Logger logger = LoggerFactory.getLogger(LogVariableExtracter.class);

	public static Set<String> extractFromFile(String filename) {
		Set<String> propertyKeys = new TreeSet<String>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filename));
			int ch = -1;
			StringBuilder buf = new StringBuilder();
			while (true) {
				ch = reader.read();
				if (ch == -1) {
					break;
				}
				if (ch != '<')
					continue;

				while ((ch = reader.read()) > -1) {
					if (ch == '<') {
						buf.delete(0, buf.length());
						continue;
					} else if (ch == '>')
						break;
					buf.append((char) ch);
				}

				propertyKeys.add(buf.toString());
				buf.delete(0, buf.length());

			}

		} catch (FileNotFoundException e) {
			logger.error("kraken syslog parser: file not found [{}]", filename);
		} catch (IOException e) {
			logger.error("kraken syslog parser: io error", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				reader = null;
			}
		}

		return propertyKeys;
	}
}
