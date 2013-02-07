/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.logdb.cmd;

import static org.krakenapps.bnf.Syntax.k;
import static org.krakenapps.bnf.Syntax.ref;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Map;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.ParserContext;
import org.krakenapps.bnf.Placeholder;
import org.krakenapps.bnf.Result;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQueryParser;

public class CmdParser implements LogQueryParser {

	@Override
	public Object parse(Binding b) {
		@SuppressWarnings("unchecked")
		Map<String, String> options = (Map<String, String>) b.getChildren()[1].getValue();
		String line = (String) b.getChildren()[2].getValue();

		try {
			String charsetName = "utf-8";
			if (options.containsKey("charset"))
				charsetName = options.get("charset");

			int offset = 0;
			if (options.containsKey("offset"))
				offset = Integer.valueOf(options.get("offset"));

			int limit = 0;
			if (options.containsKey("limit"))
				limit = Integer.valueOf(options.get("limit"));

			ProcessBuilder builder = new ProcessBuilder(line.split(" "));
			builder.redirectErrorStream(true);
			Process p = builder.start();

			return new Cmd(p, Charset.forName(charsetName), offset, limit);
		} catch (IOException e) {
			throw new RuntimeException("cannot start process", e);
		}
	}

	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("cmd", this, k("cmd "), ref("option"), new Drain());
		syntax.addRoot("cmd");
	}

	private static class Drain implements Placeholder {
		@Override
		public Result eval(String text, int position, ParserContext ctx) throws ParseException {
			String s = text.substring(position);
			return new Result(new Binding(this, s), text.length());
		}
	}
}
