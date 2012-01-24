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
import java.util.ArrayList;

public class SequenceRule implements Rule {
	private Rule[] rules;

	public SequenceRule(Rule... rules) {
		this.rules = rules;
	}

	public Rule[] getRules() {
		return rules;
	}

	@Override
	public Result eval(String text, int position, ParserContext ctx) throws ParseException {
		ArrayList<Binding> args = new ArrayList<Binding>();

		for (Rule rule : rules) {
			Result ret = rule.eval(text, position, ctx);
			if (ret.binding != null)
				args.add(ret.binding);
			
			position = ret.nextPosition;
		}

		if (args.size() == 1)
			return new Result(args.get(0), position);

		Binding[] b = new Binding[args.size()];
		args.toArray(b);
		return new Result(new Binding(this, b), position);
	}

	@Override
	public String toString() {
		return "seq";
	}
}
