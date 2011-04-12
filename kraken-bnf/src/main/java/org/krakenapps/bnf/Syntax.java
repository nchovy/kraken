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
import java.util.List;

public class Syntax {
	private RuleTable table;

	public Syntax() {
		table = new RuleTable();
	}

	public void addRoot(String target) {
		table.addRoot(target);
	}

	public void add(String target, Parser ob, Rule... rules) {
		int count = 0;
		for (int i = 0; i < rules.length; i++) {
			transform(target, rules, i, count);
			count++;
		}

		if (rules.length == 1)
			table.add(target, rules[0], ob);
		else
			table.add(target, new SequenceRule(rules), ob);
	}

	private void transform(String target, Rule[] rules, int i, int count) {
		Rule r = rules[i];

		if (r instanceof RepetitionRule) {
			Rule t = ((RepetitionRule) r).getRule();
			String repRuleName = target + count;
			String repPrimeRuleName = repRuleName + "'";
			add(repRuleName, null, ref(repPrimeRuleName));
			add(repPrimeRuleName, null, choice(rule(t, ref(repPrimeRuleName)), new EmptyRule()));
			rules[i] = ref(repRuleName);

			transform(target, rules, i, count);
		}

		if (r instanceof OptionalRule) {
			rules[i] = choice(((OptionalRule) r).getRule(), new EmptyRule());
			transform(target, rules, i, count);
		}

		if (r instanceof SequenceRule) {
			Rule[] l = ((SequenceRule) r).getRules();
			for (int j = 0; j < l.length; j++)
				transform(target, l, j, count);
		}

		if (r instanceof ChoiceRule) {
			Rule[] l = ((ChoiceRule) r).getRules();
			for (int j = 0; j < l.length; j++)
				transform(target, l, j, count);
		}
	}

	public Object eval(String text) throws ParseException {
		return eval(text, null);
	}

	public Object eval(String text, ParserTracer tracer) throws ParseException {
		List<Rule> roots = table.getRoots();
		ParserContext ctx = new DefaultContext(table, tracer);

		for (Rule r : roots) {
			try {
				Result ret = r.eval(text, 0, ctx);
				if (ret.nextPosition != text.length())
					continue;

				return ret.binding.getValue();
			} catch (ParseException e) {
			}
		}

		throw new ParseException("invalid input", 0);
	}

	public static SequenceRule rule(Rule... rules) {
		return new SequenceRule(rules);
	}

	public static OptionalRule option(Rule... rules) {
		if (rules.length == 1)
			return new OptionalRule(rules[0]);

		return new OptionalRule(new SequenceRule(rules));
	}

	public static ChoiceRule choice(Rule... rules) {
		return new ChoiceRule(rules);
	}

	public static Literal t(String literal) {
		return new Literal(literal);
	}

	public static Literal k(String keyword) {
		return new Literal(keyword, true);
	}

	public static Reference ref(String name) {
		return new Reference(name);
	}

	public static IdentifierPlaceholder idvar() {
		return new IdentifierPlaceholder();
	}

	public static RepetitionRule repeat(Rule r) {
		return new RepetitionRule(r);
	}

	public static EmptyRule empty() {
		return new EmptyRule();
	}

	public static UnsignedIntegerPlaceholder uint() {
		return new UnsignedIntegerPlaceholder();
	}

	private static class DefaultContext implements ParserContext {
		private RuleTable table;
		private ParserTracer tracer;

		public DefaultContext(RuleTable table, ParserTracer tracer) {
			this.table = table;
			this.tracer = tracer;
		}

		@Override
		public Rule getRule(String target) {
			return table.getRule(target);
		}

		@Override
		public Parser getParser(String target) {
			return table.getParser(target);
		}

		@Override
		public ParserTracer getTracer() {
			return tracer;
		}

	}
}
