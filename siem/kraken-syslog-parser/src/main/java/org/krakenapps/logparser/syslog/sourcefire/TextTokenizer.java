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
package org.krakenapps.logparser.syslog.sourcefire;

public class TextTokenizer {
	private TextTokenizer() {
	}

	public static String[] split(String source) {
		return split(source, " ");
	}

	public static String[] split(String source, String separater) {
		String[] tokens = source.split(separater);
		int emptyCount = 0;
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].length() == 0)
				emptyCount++;
		}

		// fast return
		if (emptyCount == 0)
			return tokens;

		String[] nonEmptyTokens = new String[tokens.length - emptyCount];

		int index = 0;
		for (int i = 0; i < tokens.length; i++)
			if (tokens[i].length() > 0)
				nonEmptyTokens[index++] = tokens[i];

		return nonEmptyTokens;
	}
}