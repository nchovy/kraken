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
package org.krakenapps.linux.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {
	private static final Logger logger = LoggerFactory.getLogger(Util.class.getName());

	private Util() {
	}

	public static String run(String cmd) {
		java.lang.Process p = null;
		BufferedReader br = null;
		StringBuilder builder = new StringBuilder();

		logger.info("kraken-linux-api: execute " + cmd);
		try {
			String line = null;
			p = Runtime.getRuntime().exec(cmd);

			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = br.readLine()) != null)
				builder.append(line + "\n");
			br.close();

			br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = br.readLine()) != null)
				logger.error("kraken-linux-api: " + line);
		} catch (IOException e) {
			logger.error("kraken-linux-api: run error", e);
		} finally {
			if (p != null)
				p.destroy();
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				logger.error("kraken linux api: run error", e);
			}
		}

		return builder.toString();
	}
}
