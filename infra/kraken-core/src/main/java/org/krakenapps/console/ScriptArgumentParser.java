/*
 * Copyright 2009 NCHOVY
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
 package org.krakenapps.console;

import java.util.ArrayList;
import java.util.List;

public class ScriptArgumentParser {
	public static String[] tokenize(String line) {
		StringBuilder sb = new StringBuilder();
		List<String> args = new ArrayList<String>();

		boolean quoteOpen = false;
		boolean escape = false;

		int i = 0;
		while (true) {
			if (i >= line.length())
				break;

			char c = line.charAt(i);

			i++;

			if (c == '\\') {
				if (escape) {
					escape = false;
					sb.append(c);
				} else {
					escape = true;
				}
				continue;
			}

			if (c == '"') {
				if (escape) {
					escape = false;
					sb.append(c);
				} else {
					quoteOpen = !quoteOpen;
					if (!quoteOpen) {
						args.add(sb.toString());
						sb = new StringBuilder();
					}
				}
				continue;
			}

			if (c == ' ' && !quoteOpen) {
				String parsed = sb.toString();
				if (!parsed.trim().isEmpty())
					args.add(parsed);
				sb = new StringBuilder();
				continue;
			}

			if (c != '\\' && escape) {
				sb.append('\\');
				escape = false;
			}

			sb.append(c);
		}

		String parsed = sb.toString();
		if (!parsed.trim().isEmpty())
			args.add(sb.toString());

		return args.toArray(new String[0]);
	}
}
