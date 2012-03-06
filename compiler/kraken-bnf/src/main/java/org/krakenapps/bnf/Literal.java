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

import java.text.ParseException;

public class Literal implements Rule {
	private String token;
	private boolean isCaseInsensitive;

	public Literal(String token) {
		this.token = token;
	}

	public Literal(String token, boolean isCaseInsensitive) {
		this.token = token.toLowerCase();
		this.isCaseInsensitive = isCaseInsensitive;
	}

	@Override
	public Result eval(String text, int position, ParserContext ctx) throws ParseException {
		position = StringUtil.skipSpaces(text, position);

		if (isCaseInsensitive) {
			if (text.length() < position + token.length())
				throw new ParseException("literal eval() failed", position);
			
			for (int i = 0; i < token.length(); i++) {
				char c = text.charAt(position + i);
				if (c >= 'A' && c <= 'Z')
					c += 32;

				if (c != token.charAt(i))
					throw new ParseException("literal eval() failed", position + i);
			}

			return new Result(new Binding(this, token), position + token.length());
		}

		if (text.startsWith(token, position)) {
			return new Result(new Binding(this, token), position + token.length());
		}

		throw new ParseException("literal eval() failed", position);
	}

	@Override
	public String toString() {
		return "literal '" + token + "'";
	}

}
