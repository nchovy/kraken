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
package org.krakenapps.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class QuotedKeyValueParser {

	public static Map<String, String> parse(String line) {
		List<SimpleEntry<String, String>> parseArgs = parseArgs(line);
		HashMap<String, String> result = new HashMap<String, String>();
		for (SimpleEntry<String, String> entry : parseArgs) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	private static List<SimpleEntry<String, String>> parseArgs(String argument) {
		StringBuilder currentValue = null;
		String currentKey = null;
		LinkedList<SimpleEntry<String, String>> result = new LinkedList<SimpleEntry<String, String>>();

		// ex: k1=123 k2="asdf asdf asdf" k3= "asdf" k4="" k5="asdf" k6='asdf'
		// k7='asdf asdf' k8="hello \" stania \"" k9=asdf"asdf asdf asdf"
		// k10=asdf" asdf"
		int cur = 0;
		int end = argument.length();

		while (cur < end) {
			if (currentKey == null) {
				// find key
				int tokenEnd = argument.indexOf('=', cur);
				if (tokenEnd == -1)
					break;

				currentKey = argument.substring(cur, tokenEnd);
				int lastIndexOfSpace = currentKey.lastIndexOf(' ');
				if (lastIndexOfSpace != -1) {
					currentKey = currentKey.substring(lastIndexOfSpace + 1);
				}
				cur = tokenEnd + 1;
			} else {
				// find value
				int tokenEnd = argument.indexOf(' ', cur);
				if (tokenEnd == -1) { // end of the string
					tokenEnd = end;
				}
				int quote = findNextQuote(argument, cur);

				// quote found before space
				if (quote != -1 && quote < tokenEnd) {
					currentValue = new StringBuilder();
					char currentQuote = argument.charAt(quote);
					if (quote != cur) {
						currentValue.append(argument.substring(cur, quote));
					}
					int closingQuote = findNextQuote(argument, quote + 1, currentQuote);
					if (closingQuote == -1)
						closingQuote = end;
					currentValue.append(argument.substring(quote + 1, closingQuote).replace("\\", ""));
					// proceed to find next space
					cur = closingQuote + 1;
				} else {
					if (currentValue == null) {
						currentValue = new StringBuilder();
					}
					currentValue.append(argument.substring(cur, tokenEnd));
					result.add(new SimpleEntry<String, String>(currentKey, currentValue.toString()));
					currentKey = null;
					currentValue = null;
					cur = tokenEnd + 1;
				}
			}
		}
		if (currentKey != null && currentValue != null) {
			result.add(new SimpleEntry<String, String>(currentKey, currentValue.toString()));
		}

		return result;
	}

	private static int findNextQuote(String argument, int cur, char quote) {
		int quotePos = cur;
		do {
			quotePos = argument.indexOf(quote, quotePos);
			if (quotePos == -1 || quotePos == 0)
				break;
			if (argument.charAt(quotePos - 1) == '\\') {
				// ignore escaped quote
				quotePos = quotePos + 1;
				continue;
			}
			break;
		} while (true);
		return quotePos;
	}

	private static int findNextQuote(String argument, int cur) {
		int quotePos = cur;
		do {
			int quote1Pos = argument.indexOf('\"', quotePos);
			int quote2Pos = argument.indexOf('\'', quotePos);
			if (quote1Pos == -1 && quote2Pos == -1)
				quotePos = -1;
			else {
				quotePos = Math.min(quote1Pos == -1 ? Integer.MAX_VALUE : quote1Pos, quote2Pos == -1 ? Integer.MAX_VALUE
						: quote2Pos);
			}
			if (quotePos == -1 || quotePos == 0)
				break;
			if (argument.charAt(quotePos - 1) == '\\') {
				// ignore escaped quote
				quotePos = quotePos + 1;
				continue;
			}
			break;
		} while (true);
		return quotePos;
	}

}
