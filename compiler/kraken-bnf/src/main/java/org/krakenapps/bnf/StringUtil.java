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
package org.krakenapps.bnf;

import java.util.List;

public class StringUtil {
	public static boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	public static boolean isNumeric(char c) {
		return c >= '0' && c <= '9';
	}

	public static boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isNumeric(c);
	}

	public static int skipSpaces(String text, int position) {
		int i = position;

		while (i < text.length() && text.charAt(i) == ' ')
			i++;

		return i;
	}

	public static String join(String sep, List<?> tokens) {
		StringBuilder sb = new StringBuilder();

		int i = 0;
		for (Object token : tokens) {
			if (i != 0)
				sb.append(sep);

			sb.append(token);
			i++;
		}

		return sb.toString();
	}

}
