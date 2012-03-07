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
