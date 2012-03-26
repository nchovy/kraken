package org.krakenapps.syslog.parser.internal;

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
