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
 package org.krakenapps.sonar.passive.ids.rule;

import java.text.ParseException;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.ParserContext;
import org.krakenapps.bnf.Placeholder;
import org.krakenapps.bnf.Result;
import org.krakenapps.bnf.StringUtil;

public class OptionNamePlaceholder implements Placeholder {

	@Override
	public Result eval(String text, int position, ParserContext ctx) throws ParseException {
		int i = StringUtil.skipSpaces(text, position);
		int begin = i;

		while (i < text.length() && text.charAt(i) != ':')
			i++;

		String token = text.substring(begin, i);

		return new Result(new Binding(this, token), i);
	}

}
