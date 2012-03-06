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

public class Reference implements Rule {
	private String symbol;

	public Reference(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public Result eval(String text, int position, ParserContext ctx) throws ParseException {
		Rule rule = ctx.getRule(symbol);
		if (rule == null)
			throw new ParseException("symbol [" + symbol + "] not found", position);

		ParserTracer tracer = ctx.getTracer();
		if (tracer != null)
			tracer.begin(symbol, position);

		Result ret = null;
		try {
			ret = rule.eval(text, position, ctx);
		} catch (ParseException e) {
			if (tracer != null)
				tracer.error(symbol, position);
			throw new ParseException("parse failed: " + symbol, position);
		}

		if (tracer != null)
			tracer.end(symbol, ret.nextPosition);

		Parser parser = ctx.getParser(symbol);
		if (parser == null) {
			if (ret.binding != null)
				ret.binding.setRule(this);

			return new Result(ret.binding, ret.nextPosition);
		} else {
			Object value = parser.parse(ret.binding);
			return new Result(new Binding(this, value), ret.nextPosition);
		}
	}

	@Override
	public String toString() {
		return "ref";
	}

}
