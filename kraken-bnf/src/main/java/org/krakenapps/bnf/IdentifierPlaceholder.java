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

import java.nio.BufferUnderflowException;
import java.text.ParseException;

public class IdentifierPlaceholder implements Placeholder {

	@Override
	public Result eval(String text, int position, ParserContext ctx) throws ParseException {
		int i = StringUtil.skipSpaces(text, position);

		int begin = i;
		
		if (text.length() <= begin)
			throw new BufferUnderflowException();
		
		if (!StringUtil.isAlpha(text.charAt(begin)))
			throw new ParseException("initial character must be alphabet", i);

		i = findEnd(text, i);
		String token = text.substring(begin, i);

		// remove trailing spaces
		i = StringUtil.skipSpaces(text, i);
		return new Result(new Binding(this, token), i);
	}

	private int findEnd(String text, int position) {
		int i = position;

		while (i < text.length() && StringUtil.isAlphaNumeric(text.charAt(i)))
			i++;

		return i;
	}

	@Override
	public String toString() {
		return "id_var";
	}

}
