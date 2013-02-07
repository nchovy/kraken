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
package org.krakenapps.logparser.syslog.internal;

import java.util.HashMap;
import java.util.Map;

public class KeyValueParser {
	private KeyValueParser() {
	}

	public static Map<String, Object> parse(String line) {
		int lineLength = line.length();
		Map<String, Object> m = new HashMap<String, Object>();

		int keyBegin = 0;
		while (true) {
			int keyEnd = line.indexOf('=', keyBegin);
			if (keyEnd > 0) {
				String key = line.substring(keyBegin, keyEnd);

				if (keyEnd >= lineLength - 1) {
					m.put(key, null);
				} else if (line.charAt(keyEnd + 1) == '\"') {
					int valueBegin = keyEnd + 2;
					int valueEnd = line.indexOf('\"', keyEnd + 2);
					String value = line.substring(valueBegin, valueEnd);
					m.put(key, value);

					// skip spaces
					keyBegin = valueEnd + 1;
					if (keyBegin >= lineLength - 1)
						break;

					while (line.charAt(keyBegin) == ' ')
						keyBegin++;
				} else {
					int valueBegin = keyBegin + key.length() + 1;
					int valueEnd = line.indexOf(' ', valueBegin);
					String value = line.substring(valueBegin, valueEnd);
					m.put(key, value);

					// skip spaces
					keyBegin = valueEnd + 1;
					if (keyBegin >= lineLength - 1)
						break;

					while (line.charAt(keyBegin) == ' ')
						keyBegin++;
				}

				if (keyBegin >= lineLength - 1)
					break;
			} else
				break;
		}

		return m;
	}
}
